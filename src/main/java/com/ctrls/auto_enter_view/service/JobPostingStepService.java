package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.RESUME_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.candidateList.CandidateTechStackInterviewInfoDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingEveryInfoDto;
import com.ctrls.auto_enter_view.dto.jobPostingStep.JobPostingStepDto;
import com.ctrls.auto_enter_view.dto.jobPostingStep.JobPostingStepsDto;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleParticipantsRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class JobPostingStepService {

  private final JobPostingStepRepository jobPostingStepRepository;
  private final JobPostingRepository jobPostingRepository;
  private final CompanyRepository companyRepository;
  private final CandidateListRepository candidateListRepository;
  private final ResumeRepository resumeRepository;
  private final ResumeTechStackRepository resumeTechStackRepository;
  private final InterviewScheduleParticipantsRepository interviewScheduleParticipantsRepository;

  public void createJobPostingStep(JobPostingEntity entity, JobPostingDto.Request request) {

    List<String> jobPostingStep = request.getJobPostingStep();

    List<JobPostingStepEntity> entities = jobPostingStep.stream()
        .map(e -> Request.toStepEntity(entity, e))
        .collect(Collectors.toList());

    List<JobPostingStepEntity> savedEntities = jobPostingStepRepository.saveAll(entities);
    log.info("Saved jobPostingSteps : {}", savedEntities.stream()
        .map(e -> "id: " + e.getId() + ", step: " + e.getStep())
        .collect(Collectors.toList()));
  }

  /**
   * 채용 공고 단계 전체 조회
   *
   * @param jobPostingKey
   * @return
   */
  public JobPostingStepsDto getJobPostingSteps(String jobPostingKey) {

    JobPostingEntity jobPosting = findJobPostingEntityByJobPostingKey(jobPostingKey);

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    CompanyEntity company = findCompanyByPrincipal(principal);

    verifyCompanyOwnership(company, jobPosting);

    List<JobPostingStepEntity> jobPostingSteps = jobPostingStepRepository.findAllByJobPostingKey(
        jobPostingKey);
    List<JobPostingStepDto> jobPostingStepDtoList = JobPostingStepDto.fromEntityList(
        jobPostingSteps);

    return JobPostingStepsDto.builder()
        .jobPostingKey(jobPostingKey)
        .jobPostingSteps(jobPostingStepDtoList)
        .build();
  }

  /**
   * 전체 채용 단계의 지원자 리스트 조회 : 채용단계 ID - 지원자 key, 지원자 이름, 이력서 key, 기술 스택 리스트, 면접 일시?
   *
   * @param jobPostingKey
   * @return
   */
  @Transactional(readOnly = true)
  public List<JobPostingEveryInfoDto> getCandidatesListByStepId(String jobPostingKey) {
    List<JobPostingEveryInfoDto> jobPostingEveryInfoDtoList = new ArrayList<>();

    JobPostingEntity jobPosting = findJobPostingEntityByJobPostingKey(jobPostingKey);

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    CompanyEntity company = findCompanyByPrincipal(principal);

    verifyCompanyOwnership(company, jobPosting);

    // 해당 채용 공고의 단계를 전부 가져오기
    List<JobPostingStepEntity> jobPostingStepEntityList = jobPostingStepRepository.findAllByJobPostingKey(
        jobPostingKey);

    // 단계마다 지원자 리스트를 가져오기 : candidates_list에서 지원자Key, 지원자 이름
    // -> 이력서 Key 찾기 -> 이력서 Key로 기술 스택 리스트 가져오기
    // -> interview_schedule_participants에 해당 채용 단계 ID & 지원자 Key가 존재하는지 확인
    // -> 있다면 시작일자시간을 가져와서 조합해주기
    for (JobPostingStepEntity jobPostingStepEntity : jobPostingStepEntityList) {
      List<CandidateListEntity> candidateList = getCandidateList(jobPostingKey,
          jobPostingStepEntity.getId());

      List<CandidateTechStackInterviewInfoDto> candidateTechStackInterviewInfoDtoList = new ArrayList<>();

      for (CandidateListEntity candidate : candidateList) {
        CandidateTechStackInterviewInfoDto candidateTechStackInterviewInfoDto = mapToCandidateTechStackListDto(
            candidate, jobPostingStepEntity.getId());

        candidateTechStackInterviewInfoDtoList.add(candidateTechStackInterviewInfoDto);
      }

      jobPostingEveryInfoDtoList.add(JobPostingEveryInfoDto.builder()
          .stepId(jobPostingStepEntity.getId())
          .candidateTechStackInterviewInfoDtoList(candidateTechStackInterviewInfoDtoList)
          .build()
      );
    }

    return jobPostingEveryInfoDtoList;
  }

  // 채용공고 key로 채용공고 entity 찾기
  public JobPostingEntity findJobPostingEntityByJobPostingKey(String jobPostingKey) {

    return jobPostingRepository.findByJobPostingKey(jobPostingKey)
        .orElseThrow(() -> new CustomException(JOB_POSTING_NOT_FOUND));
  }

  // 사용자 인증 정보로 회사 entity 찾기
  private CompanyEntity findCompanyByPrincipal(User principal) {

    return companyRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
  }

  // 채용 공고를 올린 회사 본인인지 확인
  private void verifyCompanyOwnership(CompanyEntity company, JobPostingEntity jobPosting) {

    if (!company.getCompanyKey().equals(jobPosting.getCompanyKey())) {
      throw new CustomException(USER_NOT_FOUND);
    }
  }

  // 해당 채용공고에 지정된 단계에 속하는 지원자 목록 조회
  private List<CandidateListEntity> getCandidateList(String jobPostingKey, Long stepId) {

    return candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId);
  }

  // 지원자의 이력서 entity 조회
  private ResumeEntity findResumeEntityByCandidateKey(String candidateKey) {

    return resumeRepository.findByCandidateKey(candidateKey)
        .orElseThrow(() -> new CustomException(RESUME_NOT_FOUND));
  }

  // 이력서의 기술 스택 조회
  private List<TechStack> findTechStackByResumeKey(String resumeKey) {

    return resumeTechStackRepository.findAllByResumeKey(resumeKey)
        .stream()
        .map(ResumeTechStackEntity::getTechStackName)
        .collect(Collectors.toList());
  }

  // CandidateListEntity & stepId -> CandidateTechStackInterviewInfoDto 매핑
  private CandidateTechStackInterviewInfoDto mapToCandidateTechStackListDto(
      CandidateListEntity candidateListEntity, Long stepId) {

    ResumeEntity resumeEntity = findResumeEntityByCandidateKey(
        candidateListEntity.getCandidateKey());

    List<TechStack> techStack = findTechStackByResumeKey(resumeEntity.getResumeKey());

    InterviewScheduleParticipantsEntity interviewScheduleParticipantsEntity = interviewScheduleParticipantsRepository.findByJobPostingStepIdAndCandidateKey(
            stepId, candidateListEntity.getCandidateKey())
        .orElseGet(InterviewScheduleParticipantsEntity::new);

    return CandidateTechStackInterviewInfoDto.builder()
        .candidateKey(candidateListEntity.getCandidateKey())
        .candidateName(candidateListEntity.getCandidateName())
        .resumeKey(resumeEntity.getResumeKey())
        .techStack(techStack)
        .startDateTime(interviewScheduleParticipantsEntity.getInterviewStartDatetime())
        .build();
  }

  public void deleteJobPostingStep(String jobPostingKey) {

    jobPostingStepRepository.deleteByJobPostingKey(jobPostingKey);
  }

  // 채용 공고 key -> 채용 단계 조회
  public List<String> getStepByJobPostingKey(String jobPostingKey) {

    List<JobPostingStepEntity> entities = jobPostingStepRepository.findByJobPostingKey(
        jobPostingKey);
    List<String> step = new ArrayList<>();

    for (JobPostingStepEntity entity : entities) {
      step.add(entity.getStep());
    }
    log.info("step 가져오기 성공 {}", step);
    return step;
  }
}