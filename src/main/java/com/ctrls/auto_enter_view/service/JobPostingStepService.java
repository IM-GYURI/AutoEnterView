package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.RESUME_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.candidateList.CandidateTechStackInterviewInfoDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingEveryInfoDto;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleParticipantsRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
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
  private final InterviewScheduleRepository interviewScheduleRepository;
  private final InterviewScheduleParticipantsRepository interviewScheduleParticipantsRepository;

  /**
   * 채용 공고 단계 생성하기
   *
   * @param entity JobPostingEntity
   * @param request JobPostingDto.Request
   */
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
   * 전체 채용 단계의 지원자 리스트 조회 채용단계 ID - 지원자 key, 지원자 이름, 이력서 key, 기술 스택 리스트, 면접 일시?
   *
   * @param jobPostingKey 채용공고 KEY
   * @return 채용 단계의 모든 정보 리스트
   * @throws CustomException USER_NOT_FOUND, JOB_POSTING_NOT_FOUND, RESUME_NOT_FOUND
   */
  @Transactional(readOnly = true)
  public List<JobPostingEveryInfoDto> getCandidatesListByStepId(UserDetails userDetails,
      String jobPostingKey) {

    List<JobPostingEveryInfoDto> jobPostingEveryInfoDtoList = new ArrayList<>();

    JobPostingEntity jobPosting = findJobPostingEntityByJobPostingKey(jobPostingKey);

    CompanyEntity company = findCompanyByPrincipal(userDetails);

    verifyCompanyOwnership(company, jobPosting);

    // 해당 채용 공고의 단계를 전부 가져오기
    List<JobPostingStepEntity> jobPostingStepEntityList = jobPostingStepRepository.findAllByJobPostingKey(
        jobPostingKey);

    // 단계마다 지원자 리스트를 가져오기 : candidates_list에서 지원자Key, 지원자 이름
    // -> 이력서 Key 찾기 -> 이력서 Key로 기술 스택 리스트 가져오기
    // -> interview_schedule_participants에 해당 채용 단계 ID & 지원자 Key가 존재하는지 확인
    // -> 있다면 시작일자시간을 가져와서 조합해주기
    for (JobPostingStepEntity jobPostingStepEntity : jobPostingStepEntityList) {
      // 과제인지 면접인지 확인하기 -> 과제라면 첫번째 면접 컬럼이 null일 것
      Optional<InterviewScheduleEntity> interviewScheduleEntity = interviewScheduleRepository.findByJobPostingStepId(
          jobPostingStepEntity.getId());

      boolean isTask = interviewScheduleEntity.isPresent()
          && interviewScheduleEntity.get().getFirstInterviewDate() == null;
      boolean isInterview = interviewScheduleEntity.isPresent()
          && interviewScheduleEntity.get().getFirstInterviewDate() != null;

      if (isTask) {
        // 과제라면
        // candidateList에서 지원자 목록 불러오기
        // 지원자 Key, 지원자 이름, 이력서 Key, 기술스택 리스트, 과제 마감일시
        List<CandidateListEntity> candidateList = getCandidateList(jobPostingKey,
            jobPostingStepEntity.getId());

        List<CandidateTechStackInterviewInfoDto> candidateTechStackInterviewInfoDtoList = new ArrayList<>();

        for (CandidateListEntity candidate : candidateList) {
          CandidateTechStackInterviewInfoDto candidateTechStackInterviewInfoDto = mapToCandidateTechStackListDtoTask(
              candidate, jobPostingStepEntity.getId());

          candidateTechStackInterviewInfoDtoList.add(candidateTechStackInterviewInfoDto);
        }

        jobPostingEveryInfoDtoList.add(JobPostingEveryInfoDto.builder()
            .stepId(jobPostingStepEntity.getId())
            .stepName(jobPostingStepEntity.getStep())
            .candidateTechStackInterviewInfoDtoList(candidateTechStackInterviewInfoDtoList)
            .build()
        );
      } else if (isInterview) {
        // 면접이라면
        // candidateList에서 지원자 목록 불러오기
        // 지원자 Key, 지원자 이름, 이력서 Key, 기술스택 리스트, 면접 시작일시
        List<CandidateListEntity> candidateList = getCandidateList(jobPostingKey,
            jobPostingStepEntity.getId());

        List<CandidateTechStackInterviewInfoDto> candidateTechStackInterviewInfoDtoList = new ArrayList<>();

        for (CandidateListEntity candidate : candidateList) {
          CandidateTechStackInterviewInfoDto candidateTechStackInterviewInfoDto = mapToCandidateTechStackListDtoInterview(
              candidate, jobPostingStepEntity.getId());

          candidateTechStackInterviewInfoDtoList.add(candidateTechStackInterviewInfoDto);
        }

        jobPostingEveryInfoDtoList.add(JobPostingEveryInfoDto.builder()
            .stepId(jobPostingStepEntity.getId())
            .stepName(jobPostingStepEntity.getStep())
            .candidateTechStackInterviewInfoDtoList(candidateTechStackInterviewInfoDtoList)
            .build()
        );
      } else {
        // 면접 일정이나 과제 일정을 아직 안 만든 단계
        // 지원자 Key, 지원자 이름, 이력서 Key, 기술스택 리스트, null
        List<CandidateListEntity> candidateList = getCandidateList(jobPostingKey,
            jobPostingStepEntity.getId());

        List<CandidateTechStackInterviewInfoDto> candidateTechStackInterviewInfoDtoList = new ArrayList<>();

        for (CandidateListEntity candidate : candidateList) {
          CandidateTechStackInterviewInfoDto candidateTechStackInterviewInfoDto = mapToCandidateTechStackListDtoNothing(
              candidate);

          candidateTechStackInterviewInfoDtoList.add(candidateTechStackInterviewInfoDto);
        }

        jobPostingEveryInfoDtoList.add(JobPostingEveryInfoDto.builder()
            .stepId(jobPostingStepEntity.getId())
            .stepName(jobPostingStepEntity.getStep())
            .candidateTechStackInterviewInfoDtoList(candidateTechStackInterviewInfoDtoList)
            .build()
        );
      }
    }

    return jobPostingEveryInfoDtoList;
  }

  /**
   * 채용공고 key로 채용공고 entity 찾기
   *
   * @param jobPostingKey 채용 공고 PK
   * @return JobPostingEntity
   * @throws CustomException JOB_POSTING_NOT_FOUND : 채용 공고를 찾을 수 없는 경우
   */
  public JobPostingEntity findJobPostingEntityByJobPostingKey(String jobPostingKey) {

    return jobPostingRepository.findByJobPostingKey(jobPostingKey)
        .orElseThrow(() -> new CustomException(JOB_POSTING_NOT_FOUND));
  }

  /**
   * 사용자 인증 정보로 회사 entity 찾기
   *
   * @param userDetails 로그인 된 사용자 정보
   * @return CompanyEntity
   * @throws CustomException USER_NOT_FOUND : 사용자를 찾을 수 없는 경우
   */
  private CompanyEntity findCompanyByPrincipal(UserDetails userDetails) {

    return companyRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
  }

  /**
   * 채용 공고를 올린 회사 본인인지 확인
   *
   * @param company CompanyEntity
   * @param jobPosting JobPostingEntity
   * @throws CustomException USER_NOT_FOUND : 사용자를 찾을 수 없는 경우
   */
  private void verifyCompanyOwnership(CompanyEntity company, JobPostingEntity jobPosting) {

    if (!company.getCompanyKey().equals(jobPosting.getCompanyKey())) {
      throw new CustomException(USER_NOT_FOUND);
    }
  }

  /**
   * 해당 채용공고에 지정된 단계에 속하는 지원자 목록 조회
   *
   * @param jobPostingKey 채용 공고 PK
   * @param stepId 채용 단계 PK
   * @return List<CandidateListEntity>
   */
  private List<CandidateListEntity> getCandidateList(String jobPostingKey, Long stepId) {

    return candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId);
  }

  /**
   * 지원자의 이력서 entity 조회
   *
   * @param candidateKey 지원자 PK
   * @return ResumeEntity
   * @throws CustomException RESUME_NOT_FOUND : 이력서를 찾을 수 없는 경우
   */
  private ResumeEntity findResumeEntityByCandidateKey(String candidateKey) {

    return resumeRepository.findByCandidateKey(candidateKey)
        .orElseThrow(() -> new CustomException(RESUME_NOT_FOUND));
  }

  /**
   * 이력서의 기술 스택 조회
   *
   * @param resumeKey 이력서 PK
   * @return List<TechStack>
   */
  private List<TechStack> findTechStackByResumeKey(String resumeKey) {

    return resumeTechStackRepository.findAllByResumeKey(resumeKey)
        .stream()
        .map(ResumeTechStackEntity::getTechStackName)
        .collect(Collectors.toList());
  }

  /**
   * 면접 : CandidateListEntity & stepId -> CandidateTechStackInterviewInfoDto 매핑
   *
   * @param candidateListEntity CandidateListEntity
   * @param stepId 채용 공고 단계 PK
   * @return CandidateTechStackInterviewInfoDto.Response
   */
  private CandidateTechStackInterviewInfoDto mapToCandidateTechStackListDtoInterview(
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
        .scheduleDateTime(interviewScheduleParticipantsEntity.getInterviewStartDatetime())
        .build();
  }

  /**
   * CandidateListEntity & stepId -> CandidateTechStackInterviewInfoDto 매핑
   *
   * @param candidateListEntity CandidateListEntity
   * @param stepId 채용 공고 단계 PK
   * @return CandidateTechStackInterviewInfoDto.Response
   */
  private CandidateTechStackInterviewInfoDto mapToCandidateTechStackListDtoTask(
      CandidateListEntity candidateListEntity, Long stepId) {

    ResumeEntity resumeEntity = findResumeEntityByCandidateKey(
        candidateListEntity.getCandidateKey());

    List<TechStack> techStack = findTechStackByResumeKey(resumeEntity.getResumeKey());

    InterviewScheduleEntity interviewScheduleEntity = interviewScheduleRepository.findByJobPostingStepId(
        stepId).orElseGet(InterviewScheduleEntity::new);

    LocalDateTime scheduleDateTime;

    if (interviewScheduleEntity.getLastInterviewDate() != null) {
      scheduleDateTime = LocalDateTime.of(interviewScheduleEntity.getLastInterviewDate(),
          LocalTime.of(23, 59, 59));
    } else {
      scheduleDateTime = null;
    }

    return CandidateTechStackInterviewInfoDto.builder()
        .candidateKey(candidateListEntity.getCandidateKey())
        .candidateName(candidateListEntity.getCandidateName())
        .resumeKey(resumeEntity.getResumeKey())
        .techStack(techStack)
        .scheduleDateTime(scheduleDateTime)
        .build();
  }

  /**
   * CandidateListEntity & stepId -> CandidateTechStackInterviewInfoDto 매핑
   *
   * @param candidateListEntity CandidateListEntity
   * @return CandidateTechStackInterviewInfoDto.Response
   */
  private CandidateTechStackInterviewInfoDto mapToCandidateTechStackListDtoNothing(
      CandidateListEntity candidateListEntity) {

    ResumeEntity resumeEntity = findResumeEntityByCandidateKey(
        candidateListEntity.getCandidateKey());

    List<TechStack> techStack = findTechStackByResumeKey(resumeEntity.getResumeKey());

    return CandidateTechStackInterviewInfoDto.builder()
        .candidateKey(candidateListEntity.getCandidateKey())
        .candidateName(candidateListEntity.getCandidateName())
        .resumeKey(resumeEntity.getResumeKey())
        .techStack(techStack)
        .scheduleDateTime(null)
        .build();
  }

  /**
   *채용 공고 단계 삭제하기
   *
   * @param jobPostingKey 채용 공고 PK
   */
  public void deleteJobPostingStep(String jobPostingKey) {

    jobPostingStepRepository.deleteByJobPostingKey(jobPostingKey);
  }

  /**
   * 채용 공고 key -> 채용 단계 조회
   *
   * @param jobPostingKey 채용 공고 PK
   * @return 채용 단계 리스트 List<step>
   */
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

  /**
   * 채용 단계 올리기
   *
   * @param currentStepId 현재 채용 공고 단계 ID (PK)
   * @param candidateKeys 지원자 PK
   * @param jobPostingKey 채용 공고 PK
   * @param userDetails 로그인 된 사용자 정보
   * @throws CustomException COMPANY_NOT_FOUND, JOB_POSTING_NOT_FOUND, NO_AUTHORITY, NEXT_STEP_NOT_FOUND, CANDIDATE_NOT_FOUND
   */
  @Transactional
  public void editStepId(Long currentStepId, List<String> candidateKeys, String jobPostingKey,
      UserDetails userDetails) {

    CompanyEntity companyEntity = companyRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));

    // 본인 회사의 채용 공고인지 확인
    JobPostingEntity jobPostingEntity = jobPostingRepository.findByJobPostingKey(jobPostingKey)
        .orElseThrow(() -> new CustomException(ErrorCode.JOB_POSTING_NOT_FOUND));

    if (!jobPostingEntity.getCompanyKey().equals(companyEntity.getCompanyKey())) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }

    // 다음 단계 ID 계산
    Long nextStepId = currentStepId + 1;

    // 다음 단계가 존재하는지 확인
    Optional<JobPostingStepEntity> nextStepOptional = jobPostingStepRepository.findByJobPostingKeyAndId(jobPostingEntity.getJobPostingKey(), nextStepId);

    if (nextStepOptional.isEmpty()) {
      throw new CustomException(ErrorCode.NEXT_STEP_NOT_FOUND);
    }

    // 지원자 정보 조회
    List<CandidateListEntity> candidateListEntities = candidateListRepository
        .findAllByCandidateKeyInAndJobPostingKey(candidateKeys, jobPostingKey);

    if (candidateListEntities.size() != candidateKeys.size()) {
      throw new CustomException(ErrorCode.CANDIDATE_NOT_FOUND);
    }

    // 지원자의 단계 ID 업데이트
    for (CandidateListEntity candidate : candidateListEntities) {
      candidate.updateJobPostingStepId(nextStepId);
    }
  }
}