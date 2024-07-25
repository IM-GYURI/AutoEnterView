package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.COMPANY_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_HAS_CANDIDATES;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_STEP_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.NO_AUTHORITY;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.MailComponent;
import com.ctrls.auto_enter_view.dto.common.JobPostingDetailDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingInfoDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class JobPostingServiceTest {

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private FilteringService filteringService;

  @Mock
  private CandidateListRepository candidateListRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private JobPostingStepRepository jobPostingStepRepository;

  @Mock
  private JobPostingTechStackService jobPostingTechStackService;

  @Mock
  private JobPostingStepService jobPostingStepService;

  @Mock
  private CandidateService candidateService;

  @Mock
  private MailComponent mailComponent;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private KeyGenerator keyGenerator;

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

  //  @Test
//  @DisplayName("채용 공고 수정 성공 테스트")
//  void testEditJobPosting() {
//    // given
//    String companyKey = "companyKey";
//    String jobPostingKey = "JobPostingKey";
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
//    JobPostingDto.Request request = JobPostingDto.Request.builder()
//        .title("edit title")
//        .jobCategory("jobCategory")
//        .career(3)
//        .workLocation("workLocation")
//        .education("education")
//        .employmentType("edit employmentType")
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
//    List<CandidateListEntity> candidateList = Arrays.asList(
//        CandidateListEntity.builder()
//            .candidateKey("candidateKey1")
//            .jobPostingKey(jobPostingKey)
//            .build(),
//        CandidateListEntity.builder()
//            .candidateKey("candidateKey2")
//            .jobPostingKey(jobPostingKey)
//            .build()
//    );
//
//    JobPostingStepEntity jobPostingStep = JobPostingStepEntity.builder()
//        .jobPostingKey(jobPostingKey)
//        .id(1L)
//        .step("서류 단계")
//        .build();
//
//    // Mocking repository methods
//    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
//        .thenReturn(Optional.of(jobPostingEntity));
//
//    when(candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(jobPostingKey, 1L))
//        .thenReturn(candidateList);
//
//    when(candidateRepository.findByCandidateKey("candidateKey1"))
//        .thenReturn(Optional.of(CandidateEntity.builder()
//            .candidateKey("candidateKey1")
//            .name("John")
//            .email("john@example.com")
//            .build()));
//    when(candidateRepository.findByCandidateKey("candidateKey2"))
//        .thenReturn(Optional.of(CandidateEntity.builder()
//            .candidateKey("candidateKey2")
//            .name("Jane")
//            .email("jane@example.com")
//            .build()));
//
//    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey))
//        .thenReturn(Optional.of(jobPostingStep));
//
//    when(companyRepository.findByEmail(userDetails.getUsername())).thenReturn(
//        Optional.of(companyEntity));
//
//    when(securityContext.getAuthentication()).thenReturn(
//        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
//            userDetails.getAuthorities()));
//
//    // Mocking mail component
//    doNothing().when(mailComponent).sendHtmlMail(anyString(), anyString(), anyString(), eq(true));
//
//    // when
//    jobPostingService.editJobPosting(jobPostingKey, request);
//
//    // then
//    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
//    verify(candidateListRepository, times(1)).findAllByJobPostingKeyAndJobPostingStepId(
//        jobPostingKey, 1L);
//    verify(mailComponent, times(2)).sendHtmlMail(anyString(), anyString(), anyString(), eq(true));
//    verify(companyRepository, times(1)).findByEmail(userDetails.getUsername());
//
//    // Verify email contents
//    verify(mailComponent, times(2)).sendHtmlMail(toCaptor.capture(), subjectCaptor.capture(),
//        textCaptor.capture(), eq(true));
//
//    List<String> capturedSubjects = subjectCaptor.getAllValues();
//    List<String> capturedTexts = textCaptor.getAllValues();
//
//    assertEquals("채용 공고 수정 알림 : edit title", capturedSubjects.get(0));
//    assertEquals("채용 공고 수정 알림 : edit title", capturedSubjects.get(1));
//
//    assertTrue(capturedTexts.get(0).contains(
//        "지원해주신 <strong>[edit title]</strong>의 공고 내용이 수정되었습니다. 확인 부탁드립니다.<br><br><a href=\"http://localhost:8080/common/job-postings/JobPostingKey\">수정된 채용 공고 확인하기</a>"));
//    assertTrue(capturedTexts.get(1).contains(
//        "지원해주신 <strong>[edit title]</strong>의 공고 내용이 수정되었습니다. 확인 부탁드립니다.<br><br><a href=\"http://localhost:8080/common/job-postings/JobPostingKey\">수정된 채용 공고 확인하기</a>"));
//  }
//
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

    //when
    jobPostingService.deleteJobPosting(userDetails, jobPostingKey);

    //then
    verify(jobPostingRepository, times(1)).deleteByJobPostingKey(jobPostingKey);
    assertEquals(companyEntity.getCompanyKey(), jobPostingEntity.getCompanyKey());

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
  void deleteFailTestJobPostingyNotFoundError() {
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


}