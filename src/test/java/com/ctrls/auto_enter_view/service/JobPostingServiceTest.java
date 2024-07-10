package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.dto.common.JobPostingDetailDto;
import com.ctrls.auto_enter_view.dto.common.MainJobPostingDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingDto;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

class JobPostingServiceTest {

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private CandidateListRepository candidateListRepository;

  @Mock
  private JobPostingStepRepository jobPostingStepRepository;

  @Mock
  private JobPostingTechStackService jobPostingTechStackService;

  @Mock
  private JobPostingStepService jobPostingStepService;

  @Mock
  private CandidateService candidateService;

  @InjectMocks
  private JobPostingService jobPostingService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("회사 키로 채용 공고 목록 조회 - 성공")
  void testGetJobPostingsByCompanyKey() {
    String companyKey = "companyKey";
    User user = new User("email", "password", new ArrayList<>());
    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .build();
    JobPostingEntity jobPostingEntity1 = new JobPostingEntity();
    JobPostingEntity jobPostingEntity2 = new JobPostingEntity();
    List<JobPostingEntity> jobPostingEntityList = Arrays.asList(jobPostingEntity1,
        jobPostingEntity2);

    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(companyEntity));
    when(jobPostingRepository.findAllByCompanyKey(companyKey)).thenReturn(jobPostingEntityList);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    List<JobPostingInfoDto> result = jobPostingService.getJobPostingsByCompanyKey(companyKey);

    verify(companyRepository, times(1)).findByEmail(user.getUsername());
    verify(jobPostingRepository, times(1)).findAllByCompanyKey(companyKey);
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("회사 키로 채용 공고 목록 조회 - 실패 : USER_NOT_FOUND 예외 발생")
  void testGetJobPostingsByCompanyKey_UserNotFound() {
    String companyKey = "companyKey12345";
    User user = new User("email", "password", new ArrayList<>());
    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
        user.getAuthorities());
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.empty());

    CustomException exception = assertThrows(CustomException.class, () -> jobPostingService.getJobPostingsByCompanyKey(companyKey));

    verify(companyRepository, times(1)).findByEmail(user.getUsername());
    assertEquals(USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 전체 조회 - 성공")
  void testGetAllJobPosting() {

    // given
    int page = 1;
    int size = 24;
    Pageable pageable = PageRequest.of(page - 1, size);
    List<JobPostingEntity> jobPostingEntities = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      jobPostingEntities.add(new JobPostingEntity());
    }
    Page<JobPostingEntity> jobPostingPage = new PageImpl<>(jobPostingEntities, pageable, 100);

    // when
    when(jobPostingRepository.findAll(pageable)).thenReturn(jobPostingPage);
    when(companyRepository.findByCompanyKey(jobPostingEntities.get(0).getCompanyKey()))
        .thenReturn(Optional.of(new CompanyEntity()));
    when(jobPostingTechStackService.getTechStackByJobPostingKey(jobPostingEntities.get(0).getJobPostingKey()))
        .thenReturn(Arrays.asList("Java", "Spring"));

    MainJobPostingDto.Response response = jobPostingService.getAllJobPosting(page, size);

    // then
    verify(jobPostingRepository, times(1)).findAll(pageable);
    verify(companyRepository, times(size)).findByCompanyKey(jobPostingEntities.get(0).getCompanyKey());
    verify(jobPostingTechStackService, times(size)).getTechStackByJobPostingKey(jobPostingEntities.get(0).getJobPostingKey());

    assertEquals(size, response.getJobPostingsList().size());
    assertEquals(5, response.getTotalPages());
    assertEquals(100, response.getTotalElements());
  }
  
  @Test
  @DisplayName("채용 공고 상세 조회 - 성공")
  void testGetJobPostingDetail() {

    // given
    String jobPostingKey = "test-job-posting-key";
    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .build();
    List<String> techStack = Arrays.asList("Java", "Spring");
    List<String> step = Arrays.asList("서류 전형", "면접");

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(jobPostingEntity));
    when(jobPostingTechStackService.getTechStackByJobPostingKey(jobPostingKey)).thenReturn(techStack);
    when(jobPostingStepService.getStepByJobPostingKey(jobPostingKey)).thenReturn(step);

    // when
    JobPostingDetailDto.Response response = jobPostingService.getJobPostingDetail(jobPostingKey);

    // then
    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(jobPostingTechStackService, times(1)).getTechStackByJobPostingKey(jobPostingKey);
    verify(jobPostingStepService, times(1)).getStepByJobPostingKey(jobPostingKey);
    assertEquals(jobPostingEntity.getJobPostingKey(), response.getJobPostingKey());
    assertEquals(techStack, response.getTechStack());
    assertEquals(step, response.getStep());
  }

  @Test
  @DisplayName("채용 공고 지원 - 성공")
  void testApplyJobPosting() {

    // given
    String jobPostingKey = "test-job-posting-key";
    String candidateKey = "test-candidate-key";
    String candidateName = "은선";
    JobPostingStepEntity firstStep = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .build();

    when(jobPostingRepository.existsByJobPostingKey(jobPostingKey)).thenReturn(true);
    when(candidateService.getCandidateNameByKey(candidateKey)).thenReturn(candidateName);
    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(firstStep);
    when(candidateListRepository.existsByCandidateKeyAndJobPostingKey(candidateKey, jobPostingKey)).thenReturn(false);

    // when
    jobPostingService.applyJobPosting(jobPostingKey, candidateKey);

    // then
    verify(jobPostingRepository, times(1)).existsByJobPostingKey(jobPostingKey);
    verify(candidateService, times(1)).getCandidateNameByKey(candidateKey);
    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
    verify(candidateListRepository, times(1)).existsByCandidateKeyAndJobPostingKey(candidateKey, jobPostingKey);

    // 다시 보기 *
    ArgumentCaptor<CandidateListEntity> candidateListCaptor = ArgumentCaptor.forClass(CandidateListEntity.class);
    verify(candidateListRepository, times(1)).save(candidateListCaptor.capture());
    CandidateListEntity savedCandidateList = candidateListCaptor.getValue();

    assertEquals(firstStep.getId(), savedCandidateList.getJobPostingStepId());
    assertEquals(jobPostingKey, savedCandidateList.getJobPostingKey());
    assertEquals(candidateKey, savedCandidateList.getCandidateKey());
    assertEquals(candidateName, savedCandidateList.getCandidateName());
  }

  @Test
  @DisplayName("채용 공고 지원 - 실패 : 채용 공고가 존재하지 않음")
  void testApplyJobPosting_JobPostingNotFound() {

    // given
    String jobPostingKey = "invalid-job-posting-key";
    String candidateKey = "test-candidate-key";

    when(jobPostingRepository.existsByJobPostingKey(jobPostingKey)).thenReturn(false);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> jobPostingService.applyJobPosting(jobPostingKey, candidateKey));

    // then
    assertEquals(ErrorCode.JOB_POSTING_NOT_FOUND, exception.getErrorCode());
    verify(jobPostingRepository, times(1)).existsByJobPostingKey(jobPostingKey);
  }

  @Test
  @DisplayName("채용 공고 지원 - 실패 : 채용 단계가 존재하지 않음")
  void testApplyJobPosting_JobPostingStepNotFound() {

    // given
    String jobPostingKey = "test-job-posting-key";
    String candidateKey = "test-candidate-key";
    String candidateName = "John Doe";

    when(jobPostingRepository.existsByJobPostingKey(jobPostingKey)).thenReturn(true);
    when(candidateService.getCandidateNameByKey(candidateKey)).thenReturn(candidateName);
    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(null);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> jobPostingService.applyJobPosting(jobPostingKey, candidateKey));

    // then
    assertEquals(ErrorCode.JOB_POSTING_STEP_NOT_FOUND, exception.getErrorCode());
    verify(jobPostingRepository, times(1)).existsByJobPostingKey(jobPostingKey);
    verify(candidateService, times(1)).getCandidateNameByKey(candidateKey);
    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
  }

  @Test
  @DisplayName("채용 공고 지원 - 실패 : 이미 지원한 공고")
  void testApplyJobPosting_AlreadyApplied() {

    // given
    String jobPostingKey = "test-job-posting-key";
    String candidateKey = "test-candidate-key";
    String candidateName = "은선";
    JobPostingStepEntity firstStep = JobPostingStepEntity.builder()
        .id(1L)
        .jobPostingKey(jobPostingKey)
        .build();

    when(jobPostingRepository.existsByJobPostingKey(jobPostingKey)).thenReturn(true);
    when(candidateService.getCandidateNameByKey(candidateKey)).thenReturn(candidateName);
    when(jobPostingStepRepository.findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey)).thenReturn(firstStep);
    when(candidateListRepository.existsByCandidateKeyAndJobPostingKey(candidateKey, jobPostingKey)).thenReturn(true);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> jobPostingService.applyJobPosting(jobPostingKey, candidateKey));

    // then
    assertEquals(ErrorCode.ALREADY_APPLIED, exception.getErrorCode());
    verify(jobPostingRepository, times(1)).existsByJobPostingKey(jobPostingKey);
    verify(candidateService, times(1)).getCandidateNameByKey(candidateKey);
    verify(jobPostingStepRepository, times(1)).findFirstByJobPostingKeyOrderByIdAsc(jobPostingKey);
    verify(candidateListRepository, times(1)).existsByCandidateKeyAndJobPostingKey(candidateKey, jobPostingKey);
  }

  @DisplayName("채용 공고 등록 성공 테스트")
  void testCreateJobPosting() {
    //given
    String companyKey = "companyKey";

    String jobPostingKey = "JobPostingKey";

    JobPostingDto.Request request = Request.builder()
        .title("title")
        .jobCategory("jobCategory")
        .career(3)
        .workLocation("workLocation")
        .education("education")
        .employmentType("employmentType")
        .salary(3000L)
        .workTime("workTime")
        .startDate(LocalDate.of(2024, 7, 15))
        .endDate(LocalDate.of(2024, 7, 20))
        .jobPostingContent("content")
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .title("title")
        .jobCategory("jobCategory")
        .career(3)
        .workLocation("workLocation")
        .education("education")
        .employmentType("employmentType")
        .salary(3000L)
        .workTime("workTime")
        .startDate(LocalDate.of(2024, 7, 15))
        .endDate(LocalDate.of(2024, 7, 20))
        .jobPostingContent("content")
        .build();

    when(jobPostingRepository.save(jobPostingEntity)).thenReturn(jobPostingEntity);

    ArgumentCaptor<JobPostingEntity> captor = ArgumentCaptor.forClass(JobPostingEntity.class);

    //when
    jobPostingService.createJobPosting(companyKey, request);

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

  }

  @Test
  @DisplayName("채용 공고 수정 성공 테스트")
  void testEditJobPosting() {
    //given
    String companyKey = "companyKey";

    String jobPostingKey = "JobPostingKey";

    JobPostingDto.Request request = Request.builder()
        .title("edit title")
        .jobCategory("jobCategory")
        .career(3)
        .workLocation("workLocation")
        .education("education")
        .employmentType("edit employmentType")
        .salary(3000L)
        .workTime("workTime")
        .startDate(LocalDate.of(2024, 7, 15))
        .endDate(LocalDate.of(2024, 7, 20))
        .jobPostingContent("content")
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .title("title")
        .jobCategory("jobCategory")
        .career(3)
        .workLocation("workLocation")
        .education("education")
        .employmentType("employmentType")
        .salary(3000L)
        .workTime("workTime")
        .startDate(LocalDate.of(2024, 7, 15))
        .endDate(LocalDate.of(2024, 7, 20))
        .jobPostingContent("content")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));

    //when
    jobPostingService.editJobPosting(jobPostingKey, request);

    //then
    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);

    //제목, 고용타입만 바뀌는것 확인
    assertEquals(request.getTitle(), jobPostingEntity.getTitle());
    assertEquals(request.getEmploymentType(), jobPostingEntity.getEmploymentType());

  }

  @Test
  @DisplayName("채용 공고 삭제 성공 테스트")
  void testDeleteJobPosting() {
    //given
    String jobPostingKey = "jobPostingKey";

    String companyKey = "companyKey";

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .title("title")
        .jobCategory("jobCategory")
        .career(3)
        .workLocation("workLocation")
        .education("education")
        .employmentType("employmentType")
        .salary(3000L)
        .workTime("workTime")
        .startDate(LocalDate.of(2024, 7, 15))
        .endDate(LocalDate.of(2024, 7, 20))
        .jobPostingContent("content")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));

    //when
    jobPostingService.deleteJobPosting(jobPostingKey);

    //then
    verify(jobPostingRepository, times(1)).deleteByJobPostingKey(jobPostingKey);


  }

}
