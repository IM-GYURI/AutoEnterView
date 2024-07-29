package com.ctrls.auto_enter_view.service;


import static com.ctrls.auto_enter_view.enums.ErrorCode.COMPANY_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_HAS_CANDIDATES;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_STEP_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.NO_AUTHORITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.KeyGenerator;
import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.common.JobPostingDetailDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.entity.AppliedJobPostingEntity;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
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
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingImageRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.JobPostingTechStackRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;


@ExtendWith(MockitoExtension.class)
class JobPostingServiceTest {

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private JobPostingTechStackRepository jobPostingTechStackRepository;

  @Mock
  private FilteringService filteringService;

  @Mock
  private CandidateListRepository candidateListRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private JobPostingStepRepository jobPostingStepRepository;

  @Mock
  private JobPostingImageRepository jobPostingImageRepository;

  @Mock
  private ApplicantRepository applicantRepository;

  @Mock
  private AppliedJobPostingRepository appliedJobPostingRepository;

  @Mock
  private MailComponent mailComponent;

  @Mock
  private KeyGenerator keyGenerator;

  @Mock
  private RedisTemplate<String, Object> redisObjectTemplate;

  @InjectMocks
  private JobPostingService jobPostingService;

  @Test
  @DisplayName("채용 공고 등록 성공 테스트")
  void testCreateJobPosting() {
    //given
    String email = "email";
    String password = "password";
    String companyKey = "companyKey";

    JobPostingDto.Request request = Request.builder()
        .title("title")
        .jobCategory(JobCategory.BACKEND)
        .career(3)
        .workLocation("workLocation")
        .education(Education.BACHELOR)
        .employmentType("employmentType")
        .salary(3000L)
        .workTime("workTime")
        .startDate(LocalDate.of(2024, 7, 15))
        .endDate(LocalDate.of(2024, 7, 20))
        .jobPostingContent("content")
        .passingNumber(10)
        .build();

    UserDetails userDetails = User.withUsername("email").password(password)
        .roles("COMPANY").build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .email(email)
        .build();

    when(companyRepository.findByCompanyKey(companyKey)).thenReturn(
        Optional.of(companyEntity));

    // Redis 관련 mock 설정
    Set<String> mockCacheKeys = new HashSet<>();
    mockCacheKeys.add("mainJobPostings:1");
    when(redisObjectTemplate.keys(anyString())).thenReturn(mockCacheKeys);

    ArgumentCaptor<JobPostingEntity> captor = ArgumentCaptor.forClass(JobPostingEntity.class);

    //when
    jobPostingService.createJobPosting(userDetails, companyKey, request);

    //then
    verify(jobPostingRepository, times(1)).save(captor.capture());
    JobPostingEntity captorValue = captor.getValue();

    assertEquals(companyKey, captorValue.getCompanyKey());
    assertEquals(request.getTitle(), captorValue.getTitle());
    assertEquals(request.getJobCategory(), captorValue.getJobCategory());
    assertEquals(request.getCareer(), captorValue.getCareer());
    assertEquals(request.getWorkLocation(), captorValue.getWorkLocation());
    assertEquals(request.getEducation(), captorValue.getEducation());
    assertEquals(request.getEmploymentType(), captorValue.getEmploymentType());
    assertEquals(request.getSalary(), captorValue.getSalary());
    assertEquals(request.getWorkTime(), captorValue.getWorkTime());
    assertEquals(request.getStartDate(), captorValue.getStartDate());
    assertEquals(request.getEndDate(), captorValue.getEndDate());
    assertEquals(request.getJobPostingContent(), captorValue.getJobPostingContent());
    assertEquals(request.getPassingNumber(), captorValue.getPassingNumber());

    // 캐시 무효화 검증
    verify(redisObjectTemplate, times(1)).keys("mainJobPostings:*");
    verify(redisObjectTemplate, times(1)).delete(mockCacheKeys);
  }

  @Test
  @DisplayName("채용 공고 등록 실패 테스트 -> COMPANY_NOT_FOUND")
  void failTestCompanyNotFoundError() {
    // given
    String email = "email";
    String password = "password";
    String companyKey = "companyKey";

    UserDetails userDetails = User.withUsername(email).password(password)
        .roles("COMPANY").build();

    Request request = new Request();

    when(companyRepository.findByCompanyKey(companyKey)).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> jobPostingService.createJobPosting(userDetails, companyKey, request));

    // then
    verify(companyRepository, times(1)).findByCompanyKey(companyKey);

    assertEquals(ErrorCode.COMPANY_NOT_FOUND, customException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 등록 실패 테스트 -> NO_AUTHORITY")
  void failTestNoAuthorityError() {
    // given
    String email = "email";
    String password = "password";
    String companyKey = "companyKey";
    String companyName = "name";
    String companyNumber = "number";

    UserDetails userDetails = User.withUsername("email2").password(password)
        .roles("COMPANY").build();

    CompanyEntity companyEntity = new CompanyEntity(companyKey, email, password, companyName,
        companyNumber, UserRole.ROLE_COMPANY);

    Request request = new Request();

    when(companyRepository.findByCompanyKey(companyKey)).thenReturn(Optional.of(companyEntity));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> jobPostingService.createJobPosting(userDetails, companyKey, request));

    // then
    verify(companyRepository, times(1)).findByCompanyKey(companyKey);

    assertEquals(ErrorCode.NO_AUTHORITY, customException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 수정하기 : 성공")
  void editJobPosting_Success() {
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";
    String candidateKey = "candidateKey";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .title("기존 제목")
        .endDate(LocalDate.now().plusDays(7))
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("test@example.com")
        .build();

    CandidateListEntity candidateListEntity = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .build();

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .email("candidate@example.com")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    JobPostingDto.Request request = JobPostingDto.Request.builder()
        .title("기존 제목")
        .endDate(LocalDate.now().plusDays(7))
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(userDetails.getUsername()))
        .thenReturn(Optional.of(companyEntity));
    when(candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(eq(jobPostingKey),
        eq(1L)))
        .thenReturn(Collections.singletonList(candidateListEntity));
    when(candidateRepository.findByCandidateKey(candidateKey))
        .thenReturn(Optional.of(candidateEntity));
    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
        .thenReturn(Optional.of(jobPostingStepEntity));

    doNothing().when(filteringService).unscheduleResumeScoringJob(jobPostingKey);
    doNothing().when(filteringService)
        .scheduleResumeScoringJob(jobPostingKey, request.getEndDate());
    doNothing().when(mailComponent)
        .sendHtmlMail(anyString(), anyString(), anyString(), anyBoolean());

    // Redis 캐시 관련 설정 추가
    Set<String> mockCacheKeys = new HashSet<>();
    mockCacheKeys.add("mainJobPostings:1");
    when(redisObjectTemplate.keys(anyString())).thenReturn(mockCacheKeys);

    jobPostingService.editJobPosting(userDetails, jobPostingKey, request);

    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(companyRepository, times(1)).findByEmail(userDetails.getUsername());
    verify(candidateListRepository, times(1)).findAllByJobPostingKeyAndJobPostingStepId(
        eq(jobPostingKey), eq(1L));
    verify(filteringService, times(1)).unscheduleResumeScoringJob(jobPostingKey);
    verify(filteringService, times(1)).scheduleResumeScoringJob(jobPostingKey,
        request.getEndDate());
    verify(mailComponent, times(1)).sendHtmlMail(
        eq("candidate@example.com"),
        eq("채용 공고 수정 알림 : " + jobPostingEntity.getTitle()),
        contains("지원해주신 <strong>[" + jobPostingEntity.getTitle() + "]</strong>의 공고 내용이 수정되었습니다."),
        eq(true)
    );

    // Redis 캐시 무효화 검증 추가
    verify(redisObjectTemplate, times(1)).keys("mainJobPostings:*");
    verify(redisObjectTemplate, times(1)).delete(mockCacheKeys);

  }

  @Test
  @DisplayName("채용 공고 수정하기 : 실패 - JOB_POSTING_NOT_FOUND")
  void editJobPosting_JobPostingNotFoundFailure() {
    String jobPostingKey = "jobPostingKey";
    UserDetails userDetails = mock(UserDetails.class);

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        jobPostingService.editJobPosting(userDetails, jobPostingKey, new JobPostingDto.Request())
    );

    assertEquals(JOB_POSTING_NOT_FOUND, thrownException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 수정하기 : 실패 - COMPANY_NOT_FOUND")
  void editJobPosting_CompanyNotFoundFailure() {
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
        .thenReturn(Optional.of(jobPostingStepEntity));

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(userDetails.getUsername()))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () ->
        jobPostingService.editJobPosting(userDetails, jobPostingKey, new JobPostingDto.Request())
    );

    assertEquals(COMPANY_NOT_FOUND, thrownException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 수정하기 : 실패 - NO_AUTHORITY")
  void editJobPosting_NoAuthorityFailure() {
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey("differentCompanyKey")
        .title("기존 제목")
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("test@example.com")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .step("서류 단계")
        .build();

    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
        .thenReturn(Optional.of(jobPostingStepEntity));

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(userDetails.getUsername()))
        .thenReturn(Optional.of(companyEntity));

    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingService.editJobPosting(userDetails, jobPostingKey, new JobPostingDto.Request())
    );

    assertEquals(NO_AUTHORITY, exception.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 삭제 성공 테스트")
  void testDeleteJobPosting() {
    //given
    String email = "email";
    String password = "password";
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .build();

    UserDetails userDetails = User.withUsername(email)
        .password(password)
        .roles("COMPANY").build();

    when(companyRepository.findByEmail(userDetails.getUsername())).thenReturn(
        Optional.of(companyEntity));

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));

    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.of(jobPostingStepEntity));

    // Redis 캐시 관련 설정 추가
    Set<String> mockCacheKeys = new HashSet<>();
    mockCacheKeys.add("mainJobPostings:1");
    when(redisObjectTemplate.keys(anyString())).thenReturn(mockCacheKeys);

    //when
    jobPostingService.deleteJobPosting(userDetails, jobPostingKey);

    //then
    verify(jobPostingRepository, times(1)).deleteByJobPostingKey(jobPostingKey);
    assertEquals(companyEntity.getCompanyKey(), jobPostingEntity.getCompanyKey());

    // Redis 캐시 무효화 검증 추가
    verify(redisObjectTemplate, times(1)).keys("mainJobPostings:*");
    verify(redisObjectTemplate, times(1)).delete(mockCacheKeys);

  }


  @Test
  @DisplayName("채용 공고 삭제 실패 테스트 -> JOB_POSTING_HAS_CANDIDATES")
  void deleteFailTestJobPostingHasCandidatesError() {
    // given
    String email = "email";
    String password = "password";
    String jobPostingKey = "jobPostingKey";

    UserDetails userDetails = User.withUsername(email)
        .password(password)
        .roles("COMPANY").build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .build();

    Long firstStep = jobPostingStepEntity.getId();

    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.of(jobPostingStepEntity));

    when(candidateListRepository.existsByJobPostingKeyAndJobPostingStepId(jobPostingKey,
        firstStep)).thenReturn(true);

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> jobPostingService.deleteJobPosting(userDetails, jobPostingKey));

    // then
    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
    verify(candidateListRepository, times(1)).existsByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, firstStep);

    assertEquals(JOB_POSTING_HAS_CANDIDATES, customException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 삭제 실패 테스트 -> JOB_POSTING_STEP_NOT_FOUND")
  void deleteFailTestJobPostingStepNotFoundError() {
    // given
    String email = "email";
    String password = "password";
    String jobPostingKey = "jobPostingKey";

    UserDetails userDetails = User.withUsername(email)
        .password(password)
        .roles("COMPANY").build();

    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> jobPostingService.deleteJobPosting(userDetails, jobPostingKey));

    // then
    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);

    assertEquals(JOB_POSTING_STEP_NOT_FOUND, customException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 삭제 실패 테스트 -> COMPANY_NOT_FOUND")
  void deleteFailTestCompanyNotFoundError() {
    // given
    String email = "email";
    String password = "password";
    String jobPostingKey = "jobPostingKey";

    UserDetails userDetails = User.withUsername(email)
        .password(password)
        .roles("COMPANY").build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .build();

    Long firstStep = jobPostingStepEntity.getId();

    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.of(jobPostingStepEntity));

    when(candidateListRepository.existsByJobPostingKeyAndJobPostingStepId(jobPostingKey,
        firstStep)).thenReturn(false);

    when(companyRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> jobPostingService.deleteJobPosting(userDetails, jobPostingKey));

    // then
    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
    verify(candidateListRepository, times(1)).existsByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, firstStep);
    verify(companyRepository, times(1)).findByEmail(userDetails.getUsername());

    assertEquals(COMPANY_NOT_FOUND, customException.getErrorCode());
  }


  @Test
  @DisplayName("채용 공고 삭제 실패 테스트 -> JOB_POSTING_NOT_FOUND")
  void deleteFailTestJobPostingNotFoundError() {
    // given
    String email = "email";
    String password = "password";
    String jobPostingKey = "jobPostingKey";

    UserDetails userDetails = User.withUsername(email)
        .password(password)
        .roles("COMPANY").build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .build();

    Long firstStep = jobPostingStepEntity.getId();

    CompanyEntity companyEntity = new CompanyEntity();

    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.of(jobPostingStepEntity));

    when(candidateListRepository.existsByJobPostingKeyAndJobPostingStepId(jobPostingKey,
        firstStep)).thenReturn(false);

    when(companyRepository.findByEmail(userDetails.getUsername())).thenReturn(
        Optional.of(companyEntity));

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.empty());

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> jobPostingService.deleteJobPosting(userDetails, jobPostingKey));

    // then
    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
    verify(candidateListRepository, times(1)).existsByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, firstStep);
    verify(companyRepository, times(1)).findByEmail(userDetails.getUsername());
    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);

    assertEquals(JOB_POSTING_NOT_FOUND, customException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 삭제 실패 테스트 -> NO_AUTHORITY")
  void deleteFailTestNoAuthorityError() {
    // given
    String email = "email";
    String password = "password";
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";

    UserDetails userDetails = User.withUsername(email)
        .password(password)
        .roles("COMPANY").build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(1L)
        .build();

    Long firstStep = jobPostingStepEntity.getId();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey("companyKey2")
        .build();

    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
        Optional.of(jobPostingStepEntity));

    when(candidateListRepository.existsByJobPostingKeyAndJobPostingStepId(jobPostingKey,
        firstStep)).thenReturn(false);

    when(companyRepository.findByEmail(userDetails.getUsername())).thenReturn(
        Optional.of(companyEntity));

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));

    // when
    CustomException customException = assertThrows(CustomException.class,
        () -> jobPostingService.deleteJobPosting(userDetails, jobPostingKey));

    // then
    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
    verify(candidateListRepository, times(1)).existsByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, firstStep);
    verify(companyRepository, times(1)).findByEmail(userDetails.getUsername());
    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);

    assertEquals(NO_AUTHORITY, customException.getErrorCode());
  }

  @Test
  @DisplayName("Main 화면 채용 공고 조회 - 성공 : 캐시 데이터가 없는 경우")
  void getAllJobPosting_success() {
    // given
    int page = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("endDate").ascending());
    LocalDate currentDate = LocalDate.now();

    JobPostingEntity jobPosting1 = JobPostingEntity.builder()
        .jobPostingKey("jobPostingKey1")
        .companyKey("companyKey1")
        .title("테스트 채용 공고 1")
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
        .jobPostingContent("상세 내용 1")
        .build();

    JobPostingEntity jobPosting2 = JobPostingEntity.builder()
        .jobPostingKey("jobPostingKey2")
        .companyKey("companyKey2")
        .title("테스트 채용 공고 2")
        .jobCategory(JobCategory.FRONTEND)
        .career(2)
        .workLocation("서울")
        .education(Education.BACHELOR)
        .employmentType("정규직")
        .salary(45000000L)
        .workTime("유연근무제")
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusDays(20))
        .passingNumber(3)
        .jobPostingContent("상세 내용 2")
        .build();

    List<JobPostingEntity> jobPostings = Arrays.asList(jobPosting1, jobPosting2);
    Page<JobPostingEntity> jobPostingPage = new PageImpl<>(jobPostings, pageable, jobPostings.size());

    CompanyEntity company1 = CompanyEntity.builder()
        .companyKey("companyKey1")
        .email("test1@company.com")
        .password("password1")
        .companyName("테스트 회사 1")
        .companyNumber("123-456-7890")
        .role(UserRole.ROLE_COMPANY)
        .build();

    CompanyEntity company2 = CompanyEntity.builder()
        .companyKey("companyKey2")
        .email("test2@company.com")
        .password("password2")
        .companyName("테스트 회사 2")
        .companyNumber("987-654-3210")
        .role(UserRole.ROLE_COMPANY)
        .build();

    List<JobPostingTechStackEntity> techStacks1 = Arrays.asList(
        JobPostingTechStackEntity.builder()
            .jobPostingKey("jobPostingKey1")
            .techName(TechStack.HTML5)
            .build(),
        JobPostingTechStackEntity.builder()
            .jobPostingKey("jobPostingKey1")
            .techName(TechStack.PYTHON)
            .build()
    );

    List<JobPostingTechStackEntity> techStacks2 = Arrays.asList(
        JobPostingTechStackEntity.builder()
            .jobPostingKey("jobPostingKey2")
            .techName(TechStack.REACT)
            .build(),
        JobPostingTechStackEntity.builder()
            .jobPostingKey("jobPostingKey2")
            .techName(TechStack.CPP)
            .build()
    );

    when(jobPostingRepository.findByEndDateGreaterThanEqual(currentDate, pageable)).thenReturn(jobPostingPage);
    when(companyRepository.findByCompanyKey("companyKey1")).thenReturn(Optional.of(company1));
    when(companyRepository.findByCompanyKey("companyKey2")).thenReturn(Optional.of(company2));
    when(jobPostingTechStackRepository.findAllByJobPostingKey("jobPostingKey1")).thenReturn(techStacks1);
    when(jobPostingTechStackRepository.findAllByJobPostingKey("jobPostingKey2")).thenReturn(techStacks2);

    ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    when(redisObjectTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(anyString())).thenReturn(null);

    // when
    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);

    // then
    assertNotNull(response);
    assertEquals(2, response.getJobPostingsList().size());
    assertEquals(1, response.getTotalPages());
    assertEquals(2, response.getTotalElements());

    MainJobPostingDto.JobPostingMainInfo firstJobPosting = response.getJobPostingsList().get(0);
    assertEquals("jobPostingKey1", firstJobPosting.getJobPostingKey());
    assertEquals("테스트 회사 1", firstJobPosting.getCompanyName());
    assertEquals("테스트 채용 공고 1", firstJobPosting.getTitle());
    assertEquals(LocalDate.now().plusDays(30), firstJobPosting.getEndDate());
    assertEquals(2, firstJobPosting.getTechStack().size());
    assertTrue(firstJobPosting.getTechStack().contains(TechStack.HTML5));
    assertTrue(firstJobPosting.getTechStack().contains(TechStack.PYTHON));

    MainJobPostingDto.JobPostingMainInfo secondJobPosting = response.getJobPostingsList().get(1);
    assertEquals("jobPostingKey2", secondJobPosting.getJobPostingKey());
    assertEquals("테스트 회사 2", secondJobPosting.getCompanyName());
    assertEquals("테스트 채용 공고 2", secondJobPosting.getTitle());
    assertEquals(LocalDate.now().plusDays(20), secondJobPosting.getEndDate());
    assertEquals(2, secondJobPosting.getTechStack().size());
    assertTrue(secondJobPosting.getTechStack().contains(TechStack.REACT));
    assertTrue(secondJobPosting.getTechStack().contains(TechStack.CPP));

    // Redis 캐시 저장 확인
    ArgumentCaptor<MainJobPostingDto.Response> responseCaptor = ArgumentCaptor.forClass(MainJobPostingDto.Response.class);
    verify(redisObjectTemplate.opsForValue()).set(
        eq("mainJobPostings:1-10"),
        responseCaptor.capture(),
        eq(30L),
        eq(TimeUnit.MINUTES)
    );

    MainJobPostingDto.Response capturedResponse = responseCaptor.getValue();
    assertEquals(response, capturedResponse);
  }

  @Test
  @DisplayName("Main 화면 채용 공고 조회 - 빈 결과 : 캐시 데이터가 없는 경우")
  void getAllJobPosting_emptyResult() {
    // given
    int page = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("endDate").ascending());
    LocalDate currentDate = LocalDate.now();

    Page<JobPostingEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

    when(jobPostingRepository.findByEndDateGreaterThanEqual(currentDate, pageable)).thenReturn(emptyPage);

    // Redis 관련 모의 객체 설정
    ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    when(redisObjectTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(anyString())).thenReturn(null);

    // when
    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);

    // then
    assertNotNull(response);
    assertTrue(response.getJobPostingsList().isEmpty());
    assertEquals(0, response.getTotalPages());
    assertEquals(0, response.getTotalElements());

    // Redis 캐시 저장 확인
    ArgumentCaptor<MainJobPostingDto.Response> responseCaptor = ArgumentCaptor.forClass(MainJobPostingDto.Response.class);
    verify(valueOperations).set(
        eq("mainJobPostings:1-10"),
        responseCaptor.capture(),
        eq(30L),
        eq(TimeUnit.MINUTES)
    );

    MainJobPostingDto.Response capturedResponse = responseCaptor.getValue();
    assertEquals(response, capturedResponse);

  }

  @Test
  @DisplayName("Main 화면 채용 공고 조회 - 성공 : 캐시 데이터가 있는 경우")
  void getAllJobPosting_success_cachedData() {
    // given
    int page = 1;
    int size = 10;
    String cacheKey = "mainJobPostings:1-10";

    MainJobPostingDto.JobPostingMainInfo cachedJobPosting = MainJobPostingDto.JobPostingMainInfo.builder()
        .jobPostingKey("cachedJobPostingKey")
        .companyName("캐시된 회사")
        .title("캐시된 채용 공고")
        .endDate(LocalDate.now().plusDays(30))
        .techStack(Arrays.asList(TechStack.JAVA, TechStack.HTML5))
        .build();

    MainJobPostingDto.Response cachedResponse = MainJobPostingDto.Response.builder()
        .jobPostingsList(Collections.singletonList(cachedJobPosting))
        .totalPages(1)
        .totalElements(1)
        .build();

    ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    when(redisObjectTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(cacheKey)).thenReturn(cachedResponse);

    // when
    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);

    // then
    assertNotNull(response);
    assertEquals(cachedResponse, response);


    verify(valueOperations).get(cacheKey);
    verify(valueOperations, never()).set(eq(cacheKey), eq(cachedResponse), eq(30L), eq(TimeUnit.MINUTES));
  }

  @Test
  @DisplayName("Main 화면 채용 공고 조회 - 빈 결과 : 캐시 데이터가 있는 경우")
  void getAllJobPosting_emptyResult_cachedData() {
    // given
    int page = 1;
    int size = 10;
    String cacheKey = "mainJobPostings:1-10";

    MainJobPostingDto.Response cachedEmptyResponse = MainJobPostingDto.Response.builder()
        .jobPostingsList(Collections.emptyList())
        .totalPages(0)
        .totalElements(0)
        .build();

    ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
    when(redisObjectTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(cacheKey)).thenReturn(cachedEmptyResponse);

    // when
    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);

    // then
    assertNotNull(response);
    assertEquals(cachedEmptyResponse, response);

    verify(valueOperations).get(cacheKey);

    verify(valueOperations, never()).set(eq(cacheKey), eq(cachedEmptyResponse), eq(30L), eq(TimeUnit.MINUTES));
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