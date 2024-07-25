package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.COMPANY_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_HAS_CANDIDATES;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_STEP_NOT_FOUND;
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
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto.Request;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.enums.Education;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.JobCategory;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import java.time.LocalDate;
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
}