package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.dto.common.JobPostingDetailDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.entity.AppliedJobPostingEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingImageEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.JobPostingTechStackEntity;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.ApplicantRepository;
import com.ctrls.auto_enter_view.repository.AppliedJobPostingRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingImageRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.JobPostingTechStackRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class JobPostingServiceTest {

  @InjectMocks
  private JobPostingService jobPostingService;

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private JobPostingTechStackRepository jobPostingTechStackRepository;

  @Mock
  private JobPostingStepRepository jobPostingStepRepository;

  @Mock
  private JobPostingImageRepository jobPostingImageRepository;

  @Mock
  private ApplicantRepository applicantRepository;

  @Mock
  private AppliedJobPostingRepository appliedJobPostingRepository;

  @Test
  @DisplayName("Main 화면 채용 공고 조회 - 성공")
  void getAllJobPosting_success() {
    // given
    int page = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(page - 1, size);
    LocalDate currentDate = LocalDate.now();

    JobPostingEntity jobPosting = JobPostingEntity.builder()
        .jobPostingKey("jobPostingKey")
        .companyKey("companyKey")
        .title("테스트 채용 공고")
        .jobCategory(JobCategory.BACKEND)
        .career(3)
        .workLocation("서울")
        .education(Education.BACHELOR)
        .employmentType("정규직")
        .salary(50000000L)
        .workTime("유연근무제")
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusDays(30))
        .passingNumber(5)
        .jobPostingContent("상세 내용")
        .build();

    List<JobPostingEntity> jobPostings = Collections.singletonList(jobPosting);
    Page<JobPostingEntity> jobPostingPage = new PageImpl<>(jobPostings, pageable, jobPostings.size());

    CompanyEntity company = CompanyEntity.builder()
        .companyKey("companyKey")
        .email("test@company.com")
        .password("password")
        .companyName("테스트 회사")
        .companyNumber("123-456-7890")
        .role(UserRole.ROLE_COMPANY)
        .build();

    List<JobPostingTechStackEntity> techStacks = Arrays.asList(
        JobPostingTechStackEntity.builder()
            .jobPostingKey("jobPostingKey")
            .techName(TechStack.HTML5)
            .build(),
        JobPostingTechStackEntity.builder()
            .jobPostingKey("jobPostingKey")
            .techName(TechStack.PYTHON)
            .build()
    );

    when(jobPostingRepository.findByEndDateGreaterThanEqual(currentDate, pageable)).thenReturn(jobPostingPage);
    when(companyRepository.findByCompanyKey("companyKey")).thenReturn(Optional.of(company));
    when(jobPostingTechStackRepository.findAllByJobPostingKey("jobPostingKey")).thenReturn(techStacks);

    // when
    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);

    // then
    assertNotNull(response);
    assertEquals(1, response.getJobPostingsList().size());
    assertEquals(1, response.getTotalPages());
    assertEquals(1, response.getTotalElements());

    MainJobPostingDto.JobPostingMainInfo firstJobPosting = response.getJobPostingsList().get(0);
    assertEquals("jobPostingKey", firstJobPosting.getJobPostingKey());
    assertEquals("테스트 회사", firstJobPosting.getCompanyName());
    assertEquals("테스트 채용 공고", firstJobPosting.getTitle());
    assertEquals(LocalDate.now().plusDays(30), firstJobPosting.getEndDate());
    assertEquals(2, firstJobPosting.getTechStack().size());
    assertTrue(firstJobPosting.getTechStack().contains(TechStack.HTML5));
    assertTrue(firstJobPosting.getTechStack().contains(TechStack.PYTHON));
  }

  @Test
  @DisplayName("Main 화면 채용 공고 조회 - 탈퇴한 회사 처리 테스트")
  void getAllJobPosting_withWithdrawnCompany() {
    // given
    int page = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(page - 1, size);
    LocalDate currentDate = LocalDate.now();

    JobPostingEntity jobPosting = JobPostingEntity.builder()
        .jobPostingKey("jobPostingKey")
        .companyKey("withdrawnCompanyKey")
        .title("테스트 채용 공고")
        .jobCategory(JobCategory.BACKEND)
        .career(3)
        .workLocation("서울")
        .education(Education.BACHELOR)
        .employmentType("정규직")
        .salary(50000000L)
        .workTime("유연근무제")
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusDays(30))
        .passingNumber(5)
        .jobPostingContent("상세 내용")
        .build();

    List<JobPostingEntity> jobPostings = Collections.singletonList(jobPosting);
    Page<JobPostingEntity> jobPostingPage = new PageImpl<>(jobPostings, pageable, jobPostings.size());

//    List<TechStack> techStacks = Arrays.asList(TechStack.HTML5, TechStack.PYTHON);

    when(jobPostingRepository.findByEndDateGreaterThanEqual(currentDate, pageable)).thenReturn(jobPostingPage);
    when(companyRepository.findByCompanyKey("withdrawnCompanyKey")).thenReturn(Optional.empty());

    // when
    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);

    // then
    assertNotNull(response);
    assertEquals(0, response.getJobPostingsList().size());
    assertEquals(0, response.getTotalPages());
    assertEquals(0, response.getTotalElements());
  }

  @Test
  @DisplayName("채용 공고 상세 조회 테스트 - 성공")
  void getJobPostingDetail_success() {
    // given
    String jobPostingKey = "jobPostingKey";
    LocalDate currentDate = LocalDate.now();

    JobPostingEntity jobPosting = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey("companyKey")
        .title("테스트 채용 공고")
        .jobCategory(JobCategory.BACKEND)
        .career(3)
        .workLocation("서울")
        .education(Education.BACHELOR)
        .employmentType("정규직")
        .salary(50000000L)
        .workTime("유연근무제")
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusDays(30))
        .passingNumber(5)
        .jobPostingContent("상세 내용")
        .build();

    List<JobPostingTechStackEntity> techStacks = Arrays.asList(
        JobPostingTechStackEntity.builder()
            .jobPostingKey(jobPostingKey)
            .techName(TechStack.HTML5)
            .build(),
        JobPostingTechStackEntity.builder()
            .jobPostingKey(jobPostingKey)
            .techName(TechStack.PYTHON)
            .build()
    );

    List<JobPostingStepEntity> steps = Arrays.asList(
        JobPostingStepEntity.builder()
            .jobPostingKey(jobPostingKey)
            .step("서류 단계")
            .build(),
        JobPostingStepEntity.builder()
            .jobPostingKey(jobPostingKey)
            .step("과제 단계")
            .build()
    );

    JobPostingImageEntity imageEntity = JobPostingImageEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyImageUrl("http://example.com/image.jpg")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(jobPosting));
    when(jobPostingRepository.existsByJobPostingKeyAndEndDateGreaterThanEqual(jobPostingKey, currentDate)).thenReturn(true);
    when(jobPostingTechStackRepository.findAllByJobPostingKey(jobPostingKey)).thenReturn(techStacks);
    when(jobPostingStepRepository.findByJobPostingKey(jobPostingKey)).thenReturn(steps);
    when(jobPostingImageRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(imageEntity));

    // when
    JobPostingDetailDto.Response response = jobPostingService.getJobPostingDetail(jobPostingKey);

    // then
    assertNotNull(response);
    assertEquals("테스트 채용 공고", response.getTitle());
    assertEquals(JobCategory.BACKEND, response.getJobCategory());
    assertEquals(3, response.getCareer());
    assertEquals("서울", response.getWorkLocation());
    assertEquals(Education.BACHELOR, response.getEducation());
    assertEquals("정규직", response.getEmploymentType());
    assertEquals(50000000L, response.getSalary());
    assertEquals("유연근무제", response.getWorkTime());
    assertEquals(LocalDate.now().plusDays(30), response.getEndDate());
    assertEquals("상세 내용", response.getJobPostingContent());
    assertEquals(2, response.getTechStack().size());
    assertTrue(response.getTechStack().contains(TechStack.HTML5));
    assertTrue(response.getTechStack().contains(TechStack.PYTHON));
    assertEquals(2, response.getStep().size());
    assertTrue(response.getStep().contains("서류 단계"));
    assertTrue(response.getStep().contains("과제 단계"));
    assertEquals("http://example.com/image.jpg", response.getImage());

    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(jobPostingRepository, times(1)).existsByJobPostingKeyAndEndDateGreaterThanEqual(jobPostingKey, currentDate);
    verify(jobPostingTechStackRepository, times(1)).findAllByJobPostingKey(jobPostingKey);
    verify(jobPostingStepRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(jobPostingImageRepository, times(1)).findByJobPostingKey(jobPostingKey);
  }

  @Test
  @DisplayName("채용 공고 상세 조회 실패 테스트 - 존재하지 않는 채용 공고")
  void getJobPostingDetail_notFound() {
    // given
    String jobPostingKey = "nonexistentKey";
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.empty());

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingService.getJobPostingDetail(jobPostingKey)
    );

    // then
    assertEquals(ErrorCode.JOB_POSTING_NOT_FOUND, exception.getErrorCode());
    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
  }

  @Test
  @DisplayName("채용 공고 상세 조회 실패 테스트 - 마감일이 지난 채용 공고")
  void getJobPostingDetail_expired() {
    // given
    String jobPostingKey = "expiredKey";
    LocalDate currentDate = LocalDate.now();

    JobPostingEntity jobPosting = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("만료된 채용 공고")
        .endDate(currentDate.minusDays(1))
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(jobPosting));
    when(jobPostingRepository.existsByJobPostingKeyAndEndDateGreaterThanEqual(jobPostingKey, currentDate)).thenReturn(false);

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingService.getJobPostingDetail(jobPostingKey)
    );

    // then
    assertEquals(ErrorCode.JOB_POSTING_EXPIRED, exception.getErrorCode());
    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(jobPostingRepository, times(1)).existsByJobPostingKeyAndEndDateGreaterThanEqual(jobPostingKey, currentDate);
  }

  @Test
  @DisplayName("채용 공고 지원 테스트 - 성공")
  void applyJobPosting_success() {
    // ArgumentCaptor 선언 및 초기화
    ArgumentCaptor<ApplicantEntity> applicantCaptor = ArgumentCaptor.forClass(ApplicantEntity.class);
    ArgumentCaptor<AppliedJobPostingEntity> appliedJobPostingCaptor = ArgumentCaptor.forClass(AppliedJobPostingEntity.class);


    // given
    String jobPostingKey = "jobPostingKey";
    String candidateKey = "candidateKey";
    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("테스트 채용 공고")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(jobPostingEntity));
    when(applicantRepository.existsByCandidateKeyAndJobPostingKey(candidateKey, jobPostingKey)).thenReturn(false);

    // when
    jobPostingService.applyJobPosting(jobPostingKey, candidateKey);

    // then
    verify(applicantRepository, times(1)).save(applicantCaptor.capture());
    verify(appliedJobPostingRepository, times(1)).save(appliedJobPostingCaptor.capture());

    ApplicantEntity capturedApplicant = applicantCaptor.getValue();
    assertEquals(jobPostingKey, capturedApplicant.getJobPostingKey());
    assertEquals(candidateKey, capturedApplicant.getCandidateKey());
    assertEquals(0, capturedApplicant.getScore());

    AppliedJobPostingEntity capturedAppliedJobPosting = appliedJobPostingCaptor.getValue();
    assertEquals(jobPostingKey, capturedAppliedJobPosting.getJobPostingKey());
    assertEquals(candidateKey, capturedAppliedJobPosting.getCandidateKey());
    assertEquals(LocalDate.now(), capturedAppliedJobPosting.getStartDate());
    assertEquals("지원 완료", capturedAppliedJobPosting.getStepName());
    assertEquals("테스트 채용 공고", capturedAppliedJobPosting.getTitle());
  }

  @Test
  @DisplayName("채용 공고 지원 실패  - 존재하지 않는 채용 공고")
  void applyJobPosting_jobPostingNotFound() {
    // given
    String jobPostingKey = "nonexistentKey";
    String candidateKey = "candidateKey";

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.empty());

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingService.applyJobPosting(jobPostingKey, candidateKey)
    );

    // then
    assertEquals(ErrorCode.JOB_POSTING_NOT_FOUND, exception.getErrorCode());
    verify(applicantRepository, never()).save(argThat(applicant ->
        applicant.getJobPostingKey().equals(jobPostingKey) &&
            applicant.getCandidateKey().equals(candidateKey)
    ));
    verify(appliedJobPostingRepository, never()).save(argThat(appliedJobPosting ->
        appliedJobPosting.getJobPostingKey().equals(jobPostingKey) &&
            appliedJobPosting.getCandidateKey().equals(candidateKey)
    ));
  }

  @Test
  @DisplayName("채용 공고 지원 실패 - 지원 중복 체크")
  void applyJobPosting_alreadyApplied() {
    // given
    String jobPostingKey = "jobPostingKey";
    String candidateKey = "candidateKey";
    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .title("테스트 채용 공고")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));
    when(applicantRepository.existsByCandidateKeyAndJobPostingKey(candidateKey,
        jobPostingKey)).thenReturn(true);

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingService.applyJobPosting(jobPostingKey, candidateKey)
    );

    // then
    assertEquals(ErrorCode.ALREADY_APPLIED, exception.getErrorCode());
    verify(applicantRepository, never()).save(argThat(applicant ->
        applicant.getJobPostingKey().equals(jobPostingKey) &&
            applicant.getCandidateKey().equals(candidateKey)
    ));
    verify(appliedJobPostingRepository, never()).save(argThat(appliedJobPosting ->
        appliedJobPosting.getJobPostingKey().equals(jobPostingKey) &&
            appliedJobPosting.getCandidateKey().equals(candidateKey)
    ));
  }
}