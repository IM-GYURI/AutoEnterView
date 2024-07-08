package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_STEP_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.RESUME_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.candidateList.CandidateTechStackListDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.dto.jobPostingStep.JobPostingStepDto;
import com.ctrls.auto_enter_view.dto.jobPostingStep.JobPostingStepsDto;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
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

  public void createJobPostingStep(JobPostingEntity entity, JobPostingDto.Request request) {

    List<String> jobPostingStep = request.getJobPostingStep();

    List<JobPostingStepEntity> entities = jobPostingStep.stream()
        .map(e -> Request.toStepEntity(entity, e))
        .collect(Collectors.toList());

    jobPostingStepRepository.saveAll(entities);
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
   * 해당 채용 단계의 지원자 리스트 조회 : 지원자 key, 지원자 이름, 이력서 key, 기술 스택 리스트
   *
   * @param jobPostingKey
   * @param stepId
   * @return
   */
  @Transactional(readOnly = true)
  public List<CandidateTechStackListDto> getCandidatesListByStepId(String jobPostingKey,
      Long stepId) {

    JobPostingEntity jobPosting = findJobPostingEntityByJobPostingKey(jobPostingKey);

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    CompanyEntity company = findCompanyByPrincipal(principal);

    verifyCompanyOwnership(company, jobPosting);

    verifyJobPostingStepExists(jobPostingKey, stepId);

    List<CandidateListEntity> candidateList = getCandidateList(jobPostingKey, stepId);

    return candidateList.stream()
        .map(this::mapToCandidateTechStackListDto)
        .collect(Collectors.toList());
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

  // 채용공고에 해당 step이 존재하는지 확인
  private void verifyJobPostingStepExists(String jobPostingKey, Long stepId) {

    if (!jobPostingStepRepository.existsByIdAndJobPostingKey(stepId, jobPostingKey)) {
      throw new CustomException(JOB_POSTING_STEP_NOT_FOUND);
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
  private List<String> findTechStackByResumeKey(String resumeKey) {

    return resumeTechStackRepository.findAllByResumeKey(resumeKey)
        .stream()
        .map(ResumeTechStackEntity::getTechName)
        .collect(Collectors.toList());
  }

  // CadndiateEntity -> CandidateTechStackListDto 매핑
  private CandidateTechStackListDto mapToCandidateTechStackListDto(
      CandidateListEntity candidateListEntity) {

    ResumeEntity resumeEntity = findResumeEntityByCandidateKey(
        candidateListEntity.getCandidateKey());
    List<String> techStack = findTechStackByResumeKey(resumeEntity.getResumeKey());

    return CandidateTechStackListDto.builder()
        .candidateKey(candidateListEntity.getCandidateKey())
        .candidateName(candidateListEntity.getCandidateName())
        .resumeKey(resumeEntity.getResumeKey())
        .techStack(techStack)
        .build();
  }

  public void editJobPostingStep(String jobPostingKey, JobPostingDto.Request request) {

    List<JobPostingStepEntity> entities = jobPostingStepRepository.findByJobPostingKey(
        jobPostingKey);

    jobPostingStepRepository.deleteAll(entities);

    List<String> jobPostingStep = request.getJobPostingStep();

    List<JobPostingStepEntity> jobPostingStepEntities = jobPostingStep.stream()
        .map(e -> Request.toStepEntity(jobPostingKey, e))
        .toList();

    jobPostingStepRepository.saveAll(jobPostingStepEntities);
  }

  public void deleteJobPostingStep(String jobPostingKey) {

    jobPostingStepRepository.deleteByJobPostingKey(jobPostingKey);
  }
}