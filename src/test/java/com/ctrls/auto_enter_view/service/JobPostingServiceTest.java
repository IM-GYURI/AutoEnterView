package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.COMPANY_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.NO_AUTHORITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JobPostingServiceTest {

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private CandidateListRepository candidateListRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private JobPostingStepRepository jobPostingStepRepository;

  @Mock
  private JobPostingTechStackService jobPostingTechStackService;

  @Mock
  private FilteringService filteringService;

  @Mock
  private JobPostingStepService jobPostingStepService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private MailComponent mailComponent;

  @Mock
  private SecurityContext securityContext;

  @Captor
  private ArgumentCaptor<String> toCaptor;

  @Captor
  private ArgumentCaptor<String> subjectCaptor;

  @Captor
  private ArgumentCaptor<String> textCaptor;

  @InjectMocks
  private JobPostingService jobPostingService;

//  @Test
//  @DisplayName("회사 키로 채용 공고 목록 조회 - 성공")
//  void testGetJobPostingsByCompanyKey() {
//
//    String companyKey = "companyKey";
//    User user = new User("email", "password", new ArrayList<>());
//    CompanyEntity companyEntity = CompanyEntity.builder()
//        .companyKey(companyKey)
//        .build();
//    JobPostingEntity jobPostingEntity1 = new JobPostingEntity();
//    JobPostingEntity jobPostingEntity2 = new JobPostingEntity();
//    List<JobPostingEntity> jobPostingEntityList = Arrays.asList(jobPostingEntity1,
//        jobPostingEntity2);
//
//    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(companyEntity));
//    when(jobPostingRepository.findAllByCompanyKey(companyKey)).thenReturn(jobPostingEntityList);
//
//    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//    securityContext.setAuthentication(
//        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
//    SecurityContextHolder.setContext(securityContext);
//
//    List<JobPostingInfoDto> result = jobPostingService.getJobPostingsByCompanyKey(companyKey);
//
//    verify(companyRepository, times(1)).findByEmail(user.getUsername());
//    verify(jobPostingRepository, times(1)).findAllByCompanyKey(companyKey);
//    assertEquals(2, result.size());
//  }
//
//  @Test
//  @DisplayName("회사 키로 채용 공고 목록 조회 - 실패 : USER_NOT_FOUND 예외 발생")
//  void testGetJobPostingsByCompanyKey_UserNotFound() {
//
//    String companyKey = "companyKey12345";
//    User user = new User("email", "password", new ArrayList<>());
//    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
//        user.getAuthorities());
//    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//    securityContext.setAuthentication(authentication);
//    SecurityContextHolder.setContext(securityContext);
//
//    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.empty());
//
//    CustomException exception = assertThrows(CustomException.class,
//        () -> jobPostingService.getJobPostingsByCompanyKey(companyKey));
//
//    verify(companyRepository, times(1)).findByEmail(user.getUsername());
//    assertEquals(USER_NOT_FOUND, exception.getErrorCode());
//  }
//
//  @Test
//  @DisplayName("채용 공고 전체 조회 - 성공")
//  void testGetAllJobPosting() {
//
//    // given
//    int page = 1;
//    int size = 24;
//    Pageable pageable = PageRequest.of(page - 1, size);
//    List<JobPostingEntity> jobPostingEntities = new ArrayList<>();
//    List<TechStack> techStack = Arrays.asList(TechStack.JAVA, TechStack.SPRING_BOOT);
//
//    for (int i = 0; i < size; i++) {
//      jobPostingEntities.add(new JobPostingEntity());
//    }
//    Page<JobPostingEntity> jobPostingPage = new PageImpl<>(jobPostingEntities, pageable, 100);
//
//    // when
//    when(jobPostingRepository.findAll(pageable)).thenReturn(jobPostingPage);
//    when(companyRepository.findByCompanyKey(jobPostingEntities.get(0).getCompanyKey()))
//        .thenReturn(Optional.of(new CompanyEntity()));
//    when(jobPostingTechStackService.getTechStackByJobPostingKey(
//        jobPostingEntities.get(0).getJobPostingKey()))
//        .thenReturn(techStack);
//
//    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);
//
//    // then
//    verify(jobPostingRepository, times(1)).findAll(pageable);
//    verify(companyRepository, times(size)).findByCompanyKey(
//        jobPostingEntities.get(0).getCompanyKey());
//    verify(jobPostingTechStackService, times(size)).getTechStackByJobPostingKey(
//        jobPostingEntities.get(0).getJobPostingKey());
//
//    assertEquals(size, response.getJobPostingsList().size());
//    assertEquals(5, response.getTotalPages());
//    assertEquals(100, response.getTotalElements());
//  }
//
//  @Test
//  @DisplayName("채용 공고 상세 조회 - 성공")
//  void testGetJobPostingDetail() {
//
//    // given
//    String jobPostingKey = "test-job-posting-key";
//    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
//        .jobPostingKey(jobPostingKey)
//        .build();
//    List<TechStack> techStack = Arrays.asList(TechStack.JAVA, TechStack.SPRING_BOOT);
//    List<String> step = Arrays.asList("서류 전형", "면접");
//
//    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
//        Optional.of(jobPostingEntity));
//    when(jobPostingTechStackService.getTechStackByJobPostingKey(jobPostingKey)).thenReturn(
//        techStack);
//    when(jobPostingStepService.getStepByJobPostingKey(jobPostingKey)).thenReturn(step);
//
//    // when
//    JobPostingDetailDto.Response response = jobPostingService.getJobPostingDetail(jobPostingKey);
//
//    // then
//    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
//    verify(jobPostingTechStackService, times(1)).getTechStackByJobPostingKey(jobPostingKey);
//    verify(jobPostingStepService, times(1)).getStepByJobPostingKey(jobPostingKey);
//    assertEquals(jobPostingEntity.getJobPostingKey(), response.getJobPostingKey());
//    assertEquals(techStack, response.getTechStack());
//    assertEquals(step, response.getStep());
//  }
//
//  @Test
//  @DisplayName("채용 공고 지원 - 성공")
//  void testApplyJobPosting() {
//    // given
//    String jobPostingKey = "test-job-posting-key";
//    String candidateKey = "test-candidate-key";
//    String candidateName = "은선";
//    JobPostingStepEntity firstStep = JobPostingStepEntity.builder()
//        .id(1L)
//        .jobPostingKey(jobPostingKey)
//        .build();
//
//    when(jobPostingRepository.existsByJobPostingKey(jobPostingKey)).thenReturn(true);
//    when(candidateService.getCandidateNameByKey(candidateKey)).thenReturn(candidateName);
//    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
//        .thenReturn(Optional.of(firstStep));
//    when(candidateListRepository.existsByCandidateKeyAndJobPostingKey(candidateKey,
//        jobPostingKey)).thenReturn(false);
//
//    // when
//    jobPostingService.applyJobPosting(jobPostingKey, candidateKey);
//
//    // then
//    verify(jobPostingRepository, times(1)).existsByJobPostingKey(jobPostingKey);
//    verify(candidateService, times(1)).getCandidateNameByKey(candidateKey);
//    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
//    verify(candidateListRepository, times(1)).existsByCandidateKeyAndJobPostingKey(candidateKey,
//        jobPostingKey);
//
//    // 다시 보기 *
//    ArgumentCaptor<CandidateListEntity> candidateListCaptor = ArgumentCaptor.forClass(
//        CandidateListEntity.class);
//    verify(candidateListRepository, times(1)).save(candidateListCaptor.capture());
//    CandidateListEntity savedCandidateList = candidateListCaptor.getValue();
//
//    assertEquals(firstStep.getId(), savedCandidateList.getJobPostingStepId());
//    assertEquals(jobPostingKey, savedCandidateList.getJobPostingKey());
//    assertEquals(candidateKey, savedCandidateList.getCandidateKey());
//    assertEquals(candidateName, savedCandidateList.getCandidateName());
//  }
//
//  @Test
//  @DisplayName("채용 공고 지원 - 실패 : 채용 공고가 존재하지 않음")
//  void testApplyJobPosting_JobPostingNotFound() {
//
//    // given
//    String jobPostingKey = "invalid-job-posting-key";
//    String candidateKey = "test-candidate-key";
//
//    when(jobPostingRepository.existsByJobPostingKey(jobPostingKey)).thenReturn(false);
//
//    // when
//    CustomException exception = assertThrows(CustomException.class,
//        () -> jobPostingService.applyJobPosting(jobPostingKey, candidateKey));
//
//    // then
//    assertEquals(ErrorCode.JOB_POSTING_NOT_FOUND, exception.getErrorCode());
//    verify(jobPostingRepository, times(1)).existsByJobPostingKey(jobPostingKey);
//  }
//
//  @Test
//  @DisplayName("채용 공고 지원 - 실패 : 채용 단계가 존재하지 않음")
//  void testApplyJobPosting_JobPostingStepNotFound() {
//
//    // given
//    String jobPostingKey = "test-job-posting-key";
//    String candidateKey = "test-candidate-key";
//    String candidateName = "John Doe";
//
//    when(jobPostingRepository.existsByJobPostingKey(jobPostingKey)).thenReturn(true);
//    when(candidateService.getCandidateNameByKey(candidateKey)).thenReturn(candidateName);
//    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(
//        Optional.empty());
//
//    // when
//    CustomException exception = assertThrows(CustomException.class,
//        () -> jobPostingService.applyJobPosting(jobPostingKey, candidateKey));
//
//    // then
//    assertEquals(JOB_POSTING_STEP_NOT_FOUND, exception.getErrorCode());
//    verify(jobPostingRepository, times(1)).existsByJobPostingKey(jobPostingKey);
//    verify(candidateService, times(1)).getCandidateNameByKey(candidateKey);
//    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
//  }
//
//
//  @Test
//  @DisplayName("채용 공고 지원 - 실패 : 이미 지원한 공고")
//  void testApplyJobPosting_AlreadyApplied() {
//
//    // given
//    String jobPostingKey = "test-job-posting-key";
//    String candidateKey = "test-candidate-key";
//    String candidateName = "은선";
//    JobPostingStepEntity firstStep = JobPostingStepEntity.builder()
//        .id(1L)
//        .jobPostingKey(jobPostingKey)
//        .build();
//
//    when(jobPostingRepository.existsByJobPostingKey(jobPostingKey)).thenReturn(true);
//    when(candidateService.getCandidateNameByKey(candidateKey)).thenReturn(candidateName);
//    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
//        .thenReturn(Optional.of(firstStep));
//    when(candidateListRepository.existsByCandidateKeyAndJobPostingKey(candidateKey,
//        jobPostingKey)).thenReturn(true);
//
//    // when
//    CustomException exception = assertThrows(CustomException.class,
//        () -> jobPostingService.applyJobPosting(jobPostingKey, candidateKey));
//
//    // then
//    assertEquals(ErrorCode.ALREADY_APPLIED, exception.getErrorCode());
//    verify(jobPostingRepository, times(1)).existsByJobPostingKey(jobPostingKey);
//    verify(candidateService, times(1)).getCandidateNameByKey(candidateKey);
//    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
//    verify(candidateListRepository, times(1)).existsByCandidateKeyAndJobPostingKey(candidateKey,
//        jobPostingKey);
//  }
//
//  @DisplayName("채용 공고 등록 성공 테스트")
//  void testCreateJobPosting() {
//    //given
//    String companyKey = "companyKey";
//
//    String jobPostingKey = "JobPostingKey";
//
//    JobPostingDto.Request request = Request.builder()
//        .title("title")
//        .jobCategory("jobCategory")
//        .career(3)
//        .workLocation("workLocation")
//        .education("education")
//        .employmentType("employmentType")
//        .salary(3000L)
//        .workTime("workTime")
//        .startDate(LocalDate.of(2024, 7, 15))
//        .endDate(LocalDate.of(2024, 7, 20))
//        .jobPostingContent("content")
//        .build();
//
//    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
//        .jobPostingKey(jobPostingKey)
//        .companyKey(companyKey)
//        .title("title")
//        .jobCategory("jobCategory")
//        .career(3)
//        .workLocation("workLocation")
//        .education("education")
//        .employmentType("employmentType")
//        .salary(3000L)
//        .workTime("workTime")
//        .startDate(LocalDate.of(2024, 7, 15))
//        .endDate(LocalDate.of(2024, 7, 20))
//        .jobPostingContent("content")
//        .build();
//
//    when(jobPostingRepository.save(jobPostingEntity)).thenReturn(jobPostingEntity);
//
//    ArgumentCaptor<JobPostingEntity> captor = ArgumentCaptor.forClass(JobPostingEntity.class);
//
//    //when
//    jobPostingService.createJobPosting(companyKey, request);
//
//    //then
//    verify(jobPostingRepository, times(1)).save(captor.capture());
//    JobPostingEntity captorValue = captor.getValue();
//
//    assertEquals(companyKey, captorValue.getCompanyKey());
//    assertEquals(request.getTitle(), captorValue.getTitle());
//    assertEquals(request.getJobCategory(), captorValue.getJobCategory());
//    assertEquals(request.getCareer(), captorValue.getCareer());
//    assertEquals(request.getWorkLocation(), captorValue.getWorkLocation());
//    assertEquals(request.getEducation(), captorValue.getEducation());
//    assertEquals(request.getEmploymentType(), captorValue.getEmploymentType());
//    assertEquals(request.getSalary(), captorValue.getSalary());
//    assertEquals(request.getWorkTime(), captorValue.getWorkTime());
//    assertEquals(request.getStartDate(), captorValue.getStartDate());
//    assertEquals(request.getEndDate(), captorValue.getEndDate());
//    assertEquals(request.getJobPostingContent(), captorValue.getJobPostingContent());
//  }

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

//  @Test
//  @DisplayName("채용 공고 삭제 성공 테스트")
//  void testDeleteJobPosting() {
//    //given
//    String jobPostingKey = "jobPostingKey";
//    String companyKey = "companyKey";
//
//    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
//        .jobPostingKey(jobPostingKey)
//        .companyKey(companyKey)
//        .title("title")
//        .jobCategory("jobCategory")
//        .career(3)
//        .workLocation("workLocation")
//        .education("education")
//        .employmentType("employmentType")
//        .salary(3000L)
//        .workTime("workTime")
//        .startDate(LocalDate.of(2024, 7, 15))
//        .endDate(LocalDate.of(2024, 7, 20))
//        .jobPostingContent("content")
//        .build();
//
//    JobPostingStepEntity jobPostingStep = JobPostingStepEntity.builder()
//        .jobPostingKey(jobPostingKey)
//        .id(1L)
//        .build();
//
//    CompanyEntity companyEntity = CompanyEntity.builder()
//        .companyKey(companyKey)
//        .email("email")
//        .password("password")
//        .companyName("companyName")
//        .companyNumber("02-333-3333")
//        .role(UserRole.ROLE_COMPANY)
//        .build();
//
//    UserDetails userDetails = User.withUsername(companyEntity.getEmail())
//        .password(companyEntity.getPassword())
//        .roles("COMPANY").build();
//    SecurityContextHolder.setContext(securityContext);
//
//    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
//        .thenReturn(Optional.of(jobPostingEntity));
//    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
//        .thenReturn(Optional.of(jobPostingStep));
//    when(candidateListRepository.existsByJobPostingKeyAndJobPostingStepId(jobPostingKey, 1L))
//        .thenReturn(false);
//
//    when(companyRepository.findByEmail(userDetails.getUsername())).thenReturn(
//        Optional.of(companyEntity));
//    when(securityContext.getAuthentication()).thenReturn(
//        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
//            userDetails.getAuthorities()));
//
//    //when
//    jobPostingService.deleteJobPosting(jobPostingKey);
//
//    //then
//    verify(jobPostingRepository, times(1)).deleteByJobPostingKey(jobPostingKey);
//  }
//
//  @Test
//  @DisplayName("채용 공고 삭제 실패 테스트 - 지원자가 있는 경우")
//  void testDeleteJobPostingWithCandidates() {
//    //given
//    String jobPostingKey = "jobPostingKey";
//    JobPostingStepEntity jobPostingStep = JobPostingStepEntity.builder()
//        .jobPostingKey(jobPostingKey)
//        .id(1L)
//        .build();
//    CandidateListEntity candidate = new CandidateListEntity();
//
//    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
//        .thenReturn(Optional.of(jobPostingStep));
//    when(candidateListRepository.existsByJobPostingKeyAndJobPostingStepId(jobPostingKey, 1L))
//        .thenReturn(true);
//
//    //when, then
//    CustomException exception = assertThrows(CustomException.class, () -> {
//      jobPostingService.deleteJobPosting(jobPostingKey);
//    });
//
//    assertEquals(JOB_POSTING_HAS_CANDIDATES, exception.getErrorCode());
//    verify(jobPostingRepository, never()).deleteByJobPostingKey(jobPostingKey);
//  }
//
//  @Test
//  @DisplayName("채용 공고 삭제 실패 테스트 - 첫번째 단계가 없는 경우")
//  void testDeleteJobPostingWithoutFirstStep() {
//    //given
//    String jobPostingKey = "jobPostingKey";
//
//    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
//        .thenReturn(Optional.empty());
//
//    //when, then
//    CustomException exception = assertThrows(CustomException.class, () -> {
//      jobPostingService.deleteJobPosting(jobPostingKey);
//    });
//
//    assertEquals(JOB_POSTING_STEP_NOT_FOUND, exception.getErrorCode());
//    verify(jobPostingRepository, never()).deleteByJobPostingKey(jobPostingKey);
//  }
}