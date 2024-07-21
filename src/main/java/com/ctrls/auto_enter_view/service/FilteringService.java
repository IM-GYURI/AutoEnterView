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
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import com.ctrls.auto_enter_view.entity.ResumeCareerEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.ApplicantRepository;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.JobPostingTechStackRepository;
import com.ctrls.auto_enter_view.repository.ResumeCareerRepository;
import com.ctrls.auto_enter_view.repository.ResumeCertificateRepository;
import com.ctrls.auto_enter_view.repository.ResumeExperienceRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilteringService {

  private final Scheduler scheduler;
  private final ResumeRepository resumeRepository;
  private final JobPostingRepository jobPostingRepository;
  private final ApplicantRepository applicantRepository;
  private final ResumeTechStackRepository resumeTechStackRepository;
  private final CandidateRepository candidateRepository;
  private final CandidateListRepository candidateListRepository;
  private final JobPostingStepRepository jobPostingStepRepository;
  private final JobPostingTechStackRepository jobPostingTechStackRepository;
  private final ResumeCareerRepository resumeCareerRepository;
  private final ResumeCertificateRepository resumeCertificateRepository;
  private final ResumeExperienceRepository resumeExperienceRepository;

  @Transactional
  public void calculateResumeScore(String candidateKey, String jobPostingKey) {
    ApplicantEntity applicantEntity = applicantRepository.findByCandidateKeyAndJobPostingKey(
            candidateKey, jobPostingKey)
        .orElseThrow(() -> new CustomException(ErrorCode.APPLICANT_NOT_FOUND));

    ResumeEntity resumeEntity = resumeRepository.findByCandidateKey(candidateKey)
        .orElse(null);

    if (resumeEntity == null) {
      int totalScore = 0;
      applicantEntity.updateScore(totalScore);
      return;
    }

    JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(jobPostingKey)
        .orElseThrow(() -> new CustomException(JOB_POSTING_NOT_FOUND));

    int totalScore = calculateTotalScore(resumeEntity, jobPostingEntity);

    // 지원자 점수 수정
    applicantEntity.updateScore(totalScore);
  }

  private int calculateTotalScore(ResumeEntity resumeEntity, JobPostingEntity jobPostingEntity) {

    int totalScore = 0;
    totalScore += calculatePortfolioScore(resumeEntity);
    totalScore += calculateCertificateScore(resumeEntity.getResumeKey());
    totalScore += calculateExperienceScore(resumeEntity.getResumeKey());
    totalScore += calculateTechStackScore(resumeEntity.getResumeKey(),
        jobPostingEntity.getJobPostingKey());
    totalScore += calculateEducationScore(resumeEntity, jobPostingEntity);
    totalScore += calculateCareerScore(resumeEntity.getResumeKey(), jobPostingEntity);

    return totalScore;
  }

  // 1. 포트폴리오 있으면 score +3
  private int calculatePortfolioScore(ResumeEntity resume) {
    int score = 0;

    String portfolio = resume.getPortfolio();

    if (portfolio != null) {
      score = 3;
    }

    return score;
  }

  // 2. 자격/어학/수상 컬럼 당 score +1
  private int calculateCertificateScore(String resumeKey) {
    return resumeCertificateRepository.countByResumeKey(resumeKey);
  }

  // 3. 경험/활동/교육 컬럼 당 score +1
  private int calculateExperienceScore(String resumeKey) {
    return resumeExperienceRepository.countByResumeKey(resumeKey);
  }

  // 4. 기술 스택 : 채용공고에 올라온 것과 이력서에 작성한 것 비교해서 점수화
  private int calculateTechStackScore(String resumeKey, String jobPostingKey) {
    List<ResumeTechStackEntity> resumeTechStackEntities = resumeTechStackRepository.findTechStacksByResumeKey(
        resumeKey);
    List<JobPostingTechStackEntity> jobPostingTechStackEntities = jobPostingTechStackRepository.findTechStacksByJobPostingKey(
        jobPostingKey);

    List<TechStack> resumeTechStacks = new ArrayList<>();
    for (ResumeTechStackEntity entity : resumeTechStackEntities) {
      TechStack techStack = entity.getTechStackName();
      resumeTechStacks.add(techStack);
    }

    List<TechStack> jobTechStacks = new ArrayList<>();
    for (JobPostingTechStackEntity entity : jobPostingTechStackEntities) {
      TechStack techStack = entity.getTechName();
      jobTechStacks.add(techStack);
    }

    int score = 0;
    for (TechStack techStack : resumeTechStacks) {
      if (jobTechStacks.contains(techStack)) {
        score += 5;
      }
    }

    return score;
  }

  // 5. 학력 - 무관일 경우 전부 0점
  // 제약이 있는 경우
  private int calculateEducationScore(ResumeEntity resumeEntity,
      JobPostingEntity jobPostingEntity) {
    Education resumeEducation = resumeEntity.getScholarship();
    Education requiredEducation = jobPostingEntity.getEducation();

    if (requiredEducation == Education.NONE) {
      return 0;
    }

    if (resumeEducation.getScore() < requiredEducation.getScore()) {
      return 0;
    }

    return resumeEducation.getScore();
  }

  // 6. 경력 (지원자 경력 - 경력 = 값 * 5점) , 카테고리가 일치하는 것만 점수 산정
  private int calculateCareerScore(String resumeKey, JobPostingEntity jobPostingEntity) {

    List<ResumeCareerEntity> candidateCareerList = resumeCareerRepository.findAllByResumeKey(
        resumeKey);

    JobCategory requiredJobCategory = jobPostingEntity.getJobCategory();
    int requiredCareer = jobPostingEntity.getCareer();

    // 경력 무관인 경우
    if (requiredCareer == -1) {
      return 0;
    }

    int candidateCareer = 0;
    // 지원자 경력 계산하기 (카테고리 일치 확인)
    for (ResumeCareerEntity career : candidateCareerList) {
      if (career.getJobCategory() == requiredJobCategory) {
        candidateCareer += career.getCalculatedCareer();
      }
    }

    // 지원자의 경력이 요구 경력보다 적은 경우
    if (candidateCareer < requiredCareer) {
      return 0;
    }

    return (candidateCareer - requiredCareer + 1) * 5;
  }

  // 스코어링 + 필터링 스케줄링
  public void scheduleResumeScoringJob(String jobPostingKey, LocalDate endDate) {
    try {
      // 마감일 다음 날 자정으로 설정
      LocalDateTime filteringDateTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.MIDNIGHT);

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
