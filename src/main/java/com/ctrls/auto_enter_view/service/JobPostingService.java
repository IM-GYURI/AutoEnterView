package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;

import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingInfoDto;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobPostingService {

  private final JobPostingRepository jobPostingRepository;
  private final CompanyRepository companyRepository;

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
  private com.ctrls.auto_enter_view.dto.jobPosting.JobPostingInfoDto mapToJobPostingDto(
      JobPostingEntity jobPostingEntity) {
    return com.ctrls.auto_enter_view.dto.jobPosting.JobPostingInfoDto.builder()
        .jobPostingKey(jobPostingEntity.getJobPostingKey())
        .title(jobPostingEntity.getTitle())
        .jobCategory(jobPostingEntity.getJobCategory())
        .startDate(jobPostingEntity.getStartDate())
        .endDate(jobPostingEntity.getEndDate())
        .build();
  }
}
