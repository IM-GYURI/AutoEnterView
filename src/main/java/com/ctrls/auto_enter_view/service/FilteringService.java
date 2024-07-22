package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.CANDIDATE_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_STEP_NOT_FOUND;

import com.ctrls.auto_enter_view.component.FilteringJob;
import com.ctrls.auto_enter_view.component.ScoringJob;
import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.ApplicantRepository;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilteringService {

  private final Scheduler scheduler;
  private final JobPostingRepository jobPostingRepository;
  private final ApplicantRepository applicantRepository;
  private final CandidateRepository candidateRepository;
  private final CandidateListRepository candidateListRepository;
  private final JobPostingStepRepository jobPostingStepRepository;

  // 스코어링 + 필터링 스케줄링
  public void scheduleResumeScoringJob(String jobPostingKey, LocalDate endDate) {
    try {
//      마감일 다음 날 자정으로 설정
      LocalDateTime filteringDateTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.MIDNIGHT);
//       테스트용 5분 후 스케줄링
//      LocalDateTime filteringDateTime = LocalDateTime.now().plusMinutes(5);

      // 기존 작업이 있는지 확인하고 제거
      JobKey jobKeyA = JobKey.jobKey("resumeScoringJob", "group1");
      if (scheduler.checkExists(jobKeyA)) {
        scheduler.deleteJob(jobKeyA);
      }

      // 스코어링 스케줄링
      JobDataMap jobDataMapA = new JobDataMap();
      jobDataMapA.put("jobPostingKey", jobPostingKey);

      JobDetail jobDetailA = JobBuilder.newJob(ScoringJob.class)
          .withIdentity("resumeScoringJob", "group1")
          .setJobData(jobDataMapA)
          .build();

      SimpleTrigger triggerA = TriggerBuilder.newTrigger()
          .withIdentity("resumeScoringTrigger", "group1")
          .startAt(Date.from(filteringDateTime.atZone(ZoneId.systemDefault()).toInstant()))
          .withSchedule(
              SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
          .build();

      scheduler.scheduleJob(jobDetailA, triggerA);

      // 필터링 스케줄링
      JobKey jobKeyB = JobKey.jobKey("filteringJob", "group1");
      if (scheduler.checkExists(jobKeyB)) {
        scheduler.deleteJob(jobKeyB);
      }

      JobDataMap jobDataMapB = new JobDataMap();
      jobDataMapB.put("jobPostingKey", jobPostingKey);

      JobDetail jobDetailB = JobBuilder.newJob(FilteringJob.class)
          .withIdentity("filteringJob", "group1")
          .setJobData(jobDataMapB)
          .build();

      // 스코어링 작업이 끝난 후 1분 후에 시작
      SimpleTrigger triggerB = TriggerBuilder.newTrigger()
          .withIdentity("filteringTrigger", "group1")
          .startAt(Date.from(
              filteringDateTime.plusMinutes(1).atZone(ZoneId.systemDefault()).toInstant()))
          .withSchedule(
              SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
          .build();

      scheduler.scheduleJob(jobDetailB, triggerB);
    } catch (SchedulerException e) {
      throw new RuntimeException("Failed to schedule resume scoring job", e);
    }
  }

  // 스케줄링 취소
  public void unscheduleResumeScoringJob(String jobPostingKey) {
    try {
      // 스코어링 작업과 필터링 작업의 트리거 취소
      TriggerKey scoringTriggerKey = TriggerKey.triggerKey("resumeScoringTrigger-" + jobPostingKey,
          "group1");
      TriggerKey filteringTriggerKey = TriggerKey.triggerKey("filteringTrigger-" + jobPostingKey,
          "group1");

      // 스코어링 트리거 취소
      scheduler.unscheduleJob(scoringTriggerKey);

      // 필터링 트리거 취소
      scheduler.unscheduleJob(filteringTriggerKey);

    } catch (SchedulerException e) {
      throw new RuntimeException("Failed to unschedule jobs", e);
    }
  }

  // 지원자를 점수가 높은 순서(같다면 지원한 시간이 빠른 순서)로 정렬하여 passingNumber만큼 candidateList에 저장시키기
  public void filterCandidates(String jobPostingKey) {
    JobPostingEntity jobPosting = jobPostingRepository.findByJobPostingKey(jobPostingKey)
        .orElseThrow(() -> new CustomException(JOB_POSTING_NOT_FOUND));

    int passingNumber = jobPosting.getPassingNumber();

    List<ApplicantEntity> applicants = applicantRepository.findAllByJobPostingKey(jobPostingKey);

    // 점수가 높은 순서대로 정렬 -> 점수가 같다면 지원한 시간이 빠른 순서대로 정렬
    List<ApplicantEntity> toApplicants = applicants.stream()
        .sorted(Comparator.comparingInt(ApplicantEntity::getScore).reversed()
            .thenComparing(ApplicantEntity::getCreatedAt))
        .limit(passingNumber)
        .toList();

    JobPostingStepEntity jobPostingStepEntity = jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(
            jobPostingKey)
        .orElseThrow(() -> new CustomException(JOB_POSTING_STEP_NOT_FOUND));

    for (ApplicantEntity applicant : toApplicants) {
      CandidateEntity candidate = candidateRepository.findByCandidateKey(
              applicant.getCandidateKey())
          .orElseThrow(() -> new CustomException(CANDIDATE_NOT_FOUND));

      CandidateListEntity candidateListEntity = CandidateListEntity.builder()
          .candidateListKey(KeyGenerator.generateKey())
          .jobPostingStepId(jobPostingStepEntity.getId())
          .jobPostingKey(jobPostingKey)
          .candidateKey(candidate.getCandidateKey())
          .candidateName(candidate.getName())
          .build();

      candidateListRepository.save(candidateListEntity);
    }
  }
}
