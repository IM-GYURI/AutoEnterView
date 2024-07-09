package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.common.JobPostingDetailDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto.JobPostingMainInfo;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingInfoDto;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobPostingService {

  private final JobPostingRepository jobPostingRepository;
  private final CompanyRepository companyRepository;
  private final CandidateListRepository candidateListRepository;
  private final JobPostingTechStackService jobPostingTechStackService;

  private final CandidateService candidateService;
  private final JobPostingStepRepository jobPostingStepRepository;
  private final JobPostingStepService jobPostingStepService;

  public JobPostingEntity createJobPosting(String companyKey, Request request) {

    JobPostingEntity entity = Request.toEntity(companyKey, request);
    return jobPostingRepository.save(entity);
  }

  /**
   * 회사 본인이 등록한 채용공고 목록 조회
   *
   * @param companyKey
   * @return
   */
  public List<JobPostingInfoDto> getJobPostingsByCompanyKey(
      String companyKey) {

    User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    CompanyEntity company = findCompanyByPrincipal(principal);

    verifyCompanyOwnership(company, companyKey);

    List<JobPostingEntity> jobPostingEntityList = jobPostingRepository.findAllByCompanyKey(
        companyKey);

    return jobPostingEntityList.stream()
        .map(this::mapToJobPostingDto)
        .collect(Collectors.toList());
  }

  // 사용자 인증 정보로 회사 entity 찾기
  private CompanyEntity findCompanyByPrincipal(User principal) {

    return companyRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
  }

  // 회사 본인인지 확인
  private void verifyCompanyOwnership(CompanyEntity company, String companyKey) {

    if (!company.getCompanyKey().equals(companyKey)) {
      throw new CustomException(USER_NOT_FOUND);
    }
  }

  // JobPostingEntity -> JobPostingDto 매핑
  private JobPostingInfoDto mapToJobPostingDto(
      JobPostingEntity jobPostingEntity) {

    return JobPostingInfoDto.builder()
        .jobPostingKey(jobPostingEntity.getJobPostingKey())
        .title(jobPostingEntity.getTitle())
        .jobCategory(jobPostingEntity.getJobCategory())
        .startDate(jobPostingEntity.getStartDate())
        .endDate(jobPostingEntity.getEndDate())
        .build();
  }

  public void editJobPosting(String jobPostingKey, Request request) {

    Optional<JobPostingEntity> entity = jobPostingRepository.findByJobPostingKey(jobPostingKey);

    entity.get().updateEntity(request);
  }

  // Main 화면 채용 공고 조회
  public MainJobPostingDto.Response getAllJobPosting(int page, int size) {
    Pageable pageable = PageRequest.of(page-1, size);
    Page<JobPostingEntity> jobPostingPage = jobPostingRepository.findAll(pageable);
    List<MainJobPostingDto.JobPostingMainInfo> jobPostingMainInfoList = new ArrayList<>();

    for (JobPostingEntity entity : jobPostingPage.getContent()) {
      JobPostingMainInfo jobPostingMainInfo = createJobPostingMainInfo(entity);
      jobPostingMainInfoList.add(jobPostingMainInfo);
    }

    log.info("총 {}개의 채용 공고 조회 완료", jobPostingMainInfoList.size());
    return MainJobPostingDto.Response.builder()
        .jobPostingsList(jobPostingMainInfoList)
        .build();
  }

  // 채용 공고 상세 보기
  public JobPostingDetailDto.Response getJobPostingDetail(String jobPostingKey) {
    JobPostingEntity jobPosting = jobPostingRepository.findByJobPostingKey(jobPostingKey)
        .orElseThrow(() -> new CustomException(ErrorCode.JOB_POSTING_NOT_FOUND));

    List<String> techStack = getTechStack(jobPosting.getJobPostingKey());
    List<String> step = getStep(jobPosting.getJobPostingKey());

    return JobPostingDetailDto.Response.from(jobPosting, techStack, step);
  }

  // 전체 체용 공고 List 들어갈 정보
  private JobPostingMainInfo createJobPostingMainInfo(JobPostingEntity entity) {
    String companyName = getCompanyName(entity.getCompanyKey());
    List<String> techStack = getTechStack(entity.getJobPostingKey());

    return JobPostingMainInfo.from(entity, companyName, techStack);
  }

  // 회사 이름 가져오기
  private String getCompanyName(String companyKey) {
    CompanyEntity companyEntity = companyRepository.findByCompanyKey(companyKey)
        .orElseThrow(() -> new CustomException(ErrorCode.COMPANY_NOT_FOUND));

    String companyName = companyEntity.getCompanyName();
    log.info("회사명 조회 완료 : {}", companyName);
    return companyName;
  }

  // 기술 스택 가져오기
  private List<String> getTechStack(String jobPostingKey) {
    List<String> techStack = jobPostingTechStackService.getTechStackByJobPostingKey(jobPostingKey);
    log.info("기술 스택 조회 완료 : {}", techStack);
    return techStack;
  }

  // 채용 일정 가져오기
  private List<String> getStep(String jobPostingKey) {
    List<String> step = jobPostingStepService.getStepByJobPostingKey(jobPostingKey);
    log.info("채용 단계 조회 완료 : {}", step);
    return step;
  }


  public void deleteJobPosting(String jobPostingKey) {

    jobPostingRepository.deleteByJobPostingKey(jobPostingKey);
  }

  // 채용 공고 지원하기
  @Transactional
  public void applyJobPosting(String jobPostingKey, String candidateKey) {

    if (!jobPostingRepository.existsByJobPostingKey(jobPostingKey)) {
      throw new CustomException(ErrorCode.JOB_POSTING_NOT_FOUND);
    }

    // 이름 가져오기
    String candidateName = candidateService.getCandidateNameByKey(candidateKey);

    // 해당 채용 공고의 첫 번째 단계 가져오기
    JobPostingStepEntity firstStep = jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
    if (firstStep == null) {
      throw new CustomException(ErrorCode.JOB_POSTING_STEP_NOT_FOUND);
    }

    // 채용 지원 중복 체크
    boolean isApplied = candidateListRepository.existsByCandidateKeyAndJobPostingKey(candidateKey, jobPostingKey);
    if (isApplied) {
      throw new CustomException(ErrorCode.ALREADY_APPLIED);
    }

    CandidateListEntity candidateList = CandidateListEntity.builder()
        .candidateListKey(KeyGenerator.generateKey())
        .jobPostingStepId(firstStep.getId())
        .jobPostingKey(jobPostingKey)
        .candidateKey(candidateKey)
        .candidateName(candidateName)
        .build();

    candidateListRepository.save(candidateList);
    log.info("지원 완료 - jobPostingKey: {}, candidateKey: {}", jobPostingKey, candidateKey);
  }

}