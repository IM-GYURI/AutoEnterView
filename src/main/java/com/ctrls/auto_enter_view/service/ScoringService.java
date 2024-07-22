package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoringService {

  private final ApplicantRepository applicantRepository;
  private final JobPostingRepository jobPostingRepository;
  private final JobPostingTechStackRepository jobPostingTechStackRepository;
  private final ResumeCareerRepository resumeCareerRepository;
  private final ResumeCertificateRepository resumeCertificateRepository;
  private final ResumeExperienceRepository resumeExperienceRepository;
  private final ResumeRepository resumeRepository;
  private final ResumeTechStackRepository resumeTechStackRepository;

  @Transactional
  public void scoreApplicants(String jobPostingKey) {
    log.info("ScoringService : start scoreApplicants");

    // setup
    JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(jobPostingKey)
        .orElseThrow(() -> new CustomException(
            ErrorCode.JOB_POSTING_NOT_FOUND));

    int careerPriority = 1; // setPriority(jobPostingEntity, PriorityType.CAREER);
    int techStackPriority = 1; // setPriority(jobPostingEntity, PriorityType.TECH_STACK);
    int educationPriority = 1; // setPriority(jobPostingEntity, PriorityType.EDUCATION);

    updateScore(jobPostingEntity, careerPriority, techStackPriority, educationPriority);
  }

//  TODO: 채용공고에 우선순위 저장하기
//  private int setPriority(JobPostingEntity jobPostingEntity, PriorityType priorityType) {
//
//    int multiplier = 1;
//
//    List<PriorityType> priorities = jobPostingEntity.getPriority();
//
//    if (priorities.contains(priorityType)) {
//      multiplier = 4 - priorities.indexOf(priorityType);
//    }
//
//    return multiplier;
//  }

  private void updateScore(JobPostingEntity jobPostingEntity, int careerPriority,
      int techStackPriority, int educationPriority) {

    String jobPostingKey = jobPostingEntity.getJobPostingKey();

    EnumSet<TechStack> jobPostingTechStacks = jobPostingTechStackRepository.findAllByJobPostingKey(
            jobPostingKey).stream().map(JobPostingTechStackEntity::getTechName)
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(TechStack.class)));

    List<ApplicantEntity> applicantEntities = applicantRepository.findAllByJobPostingKey(
        jobPostingKey);

    for (ApplicantEntity applicantEntity : applicantEntities) {
      Optional<ResumeEntity> optionalResumeEntity = resumeRepository.findByCandidateKey(
          applicantEntity.getCandidateKey());

      if (optionalResumeEntity.isEmpty()) {
        continue;
      }
      int totalScore = 0;

      ResumeEntity resumeEntity = optionalResumeEntity.get();

      String resumeKey = resumeEntity.getResumeKey();

      totalScore += calculateEducationScore(jobPostingEntity, resumeEntity, educationPriority);

      totalScore += calculateExperienceScore(resumeKey);

      totalScore += calculateCertificateScore(resumeKey);

      totalScore += calculatePortfolioScore(resumeEntity);

      totalScore += calculateTechStackScore(jobPostingTechStacks, resumeKey, techStackPriority);

      totalScore += calculateCareerScore(jobPostingEntity, resumeKey, careerPriority);

      applicantEntity.updateScore(totalScore);
    }
  }

  private int calculateTechStackScore(EnumSet<TechStack> jobPostingTechStacks, String resumeKey,
      int techStackPriority) {

    int techStackScore = 5;

    EnumSet<TechStack> resumeTechStacks = resumeTechStackRepository.findAllByResumeKey(resumeKey)
        .stream().map(ResumeTechStackEntity::getTechStackName)
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(TechStack.class)));

    resumeTechStacks.retainAll(jobPostingTechStacks);

    return resumeTechStacks.size() * techStackScore * techStackPriority;
  }

  private int calculatePortfolioScore(ResumeEntity resumeEntity) {

    int portfolioScore = 3;

    String portfolio = resumeEntity.getPortfolio();

    if (portfolio == null || portfolio.isEmpty()) {
      return 0;
    } else {
      return portfolioScore;
    }
  }

  private int calculateCertificateScore(String resumeKey) {

    int certificateScore = 1;

    return resumeCertificateRepository.countAllByResumeKey(resumeKey) * certificateScore;
  }

  private int calculateExperienceScore(String resumeKey) {

    int experienceScore = 1;

    return resumeExperienceRepository.countAllByResumeKey(resumeKey) * experienceScore;
  }

  private int calculateEducationScore(JobPostingEntity jobPostingEntity,
      ResumeEntity resumeEntity, int educationPriority) {

    Education jobPostingEducation = jobPostingEntity.getEducation();
    Education resumeEducation = resumeEntity.getEducation();

    if (jobPostingEducation != Education.NONE
        && resumeEducation.compareTo(jobPostingEducation) >= 0) {
      return resumeEducation.getScore() * educationPriority;
    }

    return 0;
  }

  private int calculateCareerScore(JobPostingEntity jobPostingEntity, String resumeKey,
      int careerPriority) {

    int careerScore = 5;

    JobCategory jobPostingJobCategory = jobPostingEntity.getJobCategory();
    Integer career = jobPostingEntity.getCareer();

    if (career == -1) {
      return 0;
    } else {
      return resumeCareerRepository.findAllByResumeKey(resumeKey).stream()
          .mapToInt(e -> {
            if (e.getJobCategory() == jobPostingJobCategory) {
              return e.getCalculatedCareer() >= career ?
                  (e.getCalculatedCareer() - career + 1) * careerScore * careerPriority
                  : 0;
            } else {
              return 0;
            }
          }).sum();
    }
  }
}