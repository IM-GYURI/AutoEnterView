package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.component.ScoringJob;
import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
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
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingTechStackRepository;
import com.ctrls.auto_enter_view.repository.ResumeCareerRepository;
import com.ctrls.auto_enter_view.repository.ResumeCertificateRepository;
import com.ctrls.auto_enter_view.repository.ResumeExperienceRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
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
        .orElseThrow(() -> new CustomException(ErrorCode.JOB_POSTING_NOT_FOUND));

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

  // 스코어링 스케줄링
  public void scheduleResumeScoringJob(String jobPostingKey, LocalDate endDate) {
    try {
      // 마감일 다음 날 자정으로 설정
      LocalDateTime filteringDateTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.MIDNIGHT);

      JobDetail jobDetail = JobBuilder.newJob(ScoringJob.class)
          .withIdentity("resumeScoringJob-" + jobPostingKey, "resumeScoringGroup")
          .usingJobData("jobPostingKey", jobPostingKey)
          .build();

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity("resumeScoringTrigger-" + jobPostingKey, "resumeScoringGroup")
          .startAt(Date.from(filteringDateTime.atZone(ZoneId.systemDefault()).toInstant()))
          .withSchedule(SimpleScheduleBuilder.simpleSchedule())
          .build();

      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException e) {
      throw new RuntimeException("Failed to schedule resume scoring job", e);
    }
  }
}
