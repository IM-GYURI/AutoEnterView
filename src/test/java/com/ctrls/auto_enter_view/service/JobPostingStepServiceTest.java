package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_STEP_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.dto.candidateList.CandidateTechStackListDto;
import com.ctrls.auto_enter_view.dto.jobPostingStep.JobPostingStepsDto;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class JobPostingStepServiceTest {

  @Mock
  private JobPostingStepRepository jobPostingStepRepository;

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private CandidateListRepository candidateListRepository;

  @Mock
  private ResumeRepository resumeRepository;

  @Mock
  private ResumeTechStackRepository resumeTechStackRepository;

  @InjectMocks
  private JobPostingStepService jobPostingStepService;

  @Test
  @DisplayName("채용 공고 단계 전체 조회 - 성공")
  void testGetJobPostingSteps_Success() {
    String jobPostingKey = "jobPostingKey";
    User user = new User("email", "password", new ArrayList<>());
    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey("companyKey")
        .build();
    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey("companyKey")
        .build();
    JobPostingStepEntity stepEntity1 = new JobPostingStepEntity();
    JobPostingStepEntity stepEntity2 = new JobPostingStepEntity();
    List<JobPostingStepEntity> jobPostingSteps = List.of(stepEntity1, stepEntity2);

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(companyEntity));
    when(jobPostingStepRepository.findAllByJobPostingKey(jobPostingKey)).thenReturn(
        jobPostingSteps);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    JobPostingStepsDto result = jobPostingStepService.getJobPostingSteps(jobPostingKey);

    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(companyRepository, times(1)).findByEmail(user.getUsername());
    verify(jobPostingStepRepository, times(1)).findAllByJobPostingKey(jobPostingKey);
    assertEquals(2, result.getJobPostingSteps().size());
  }

  @Test
  @DisplayName("채용 공고 단계 전체 조회 - 실패 : JOB_POSTING_NOT_FOUND 예외 발생")
  void testGetJobPostingSteps_JobPostingNotFound() {
    String jobPostingKey = "jobPostingKey";
    User user = new User("email", "password", new ArrayList<>());

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.empty());

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    CustomException exception = assertThrows(CustomException.class, () -> {
      jobPostingStepService.getJobPostingSteps(jobPostingKey);
    });

    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    assertEquals(JOB_POSTING_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 단계 전체 조회 - 실패 : USER_NOT_FOUND 예외 발생")
  void testGetJobPostingSteps_UserNotFound() {
    String jobPostingKey = "jobPostingKey";
    User user = new User("email", "password", new ArrayList<>());
    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey("companyKey")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.empty());

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    CustomException exception = assertThrows(CustomException.class, () -> {
      jobPostingStepService.getJobPostingSteps(jobPostingKey);
    });

    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(companyRepository, times(1)).findByEmail(user.getUsername());
    assertEquals(USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("해당 채용 단계의 지원자 리스트 조회 - 성공")
  void testGetCandidatesListByStepId_Success() {
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    User user = new User("email", "password", new ArrayList<>());

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey("companyKey")
        .build();
    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey("companyKey")
        .build();
    CandidateListEntity candidateEntity = CandidateListEntity.builder()
        .candidateKey("candidateKey")
        .candidateName("name")
        .build();
    ResumeEntity resumeEntity = ResumeEntity.builder()
        .resumeKey("resumeKey")
        .build();
    ResumeTechStackEntity techStackEntity = ResumeTechStackEntity.builder()
        .techStackName(TechStack.JAVA)
        .build();

    List<CandidateListEntity> candidateList = List.of(candidateEntity);
    List<ResumeTechStackEntity> techStackList = List.of(techStackEntity);

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(companyEntity));
    when(candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(jobPostingKey,
        stepId)).thenReturn(candidateList);
    when(resumeRepository.findByCandidateKey(candidateEntity.getCandidateKey())).thenReturn(
        Optional.of(resumeEntity));
    when(resumeTechStackRepository.findAllByResumeKey(resumeEntity.getResumeKey())).thenReturn(
        techStackList);
    when(jobPostingStepRepository.existsByIdAndJobPostingKey(stepId, jobPostingKey)).thenReturn(
        true);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    List<CandidateTechStackListDto> result = jobPostingStepService.getCandidatesListByStepId(
        jobPostingKey, stepId);

    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(companyRepository, times(1)).findByEmail(user.getUsername());
    verify(candidateListRepository, times(1)).findAllByJobPostingKeyAndJobPostingStepId(
        jobPostingKey, stepId);
    verify(resumeRepository, times(1)).findByCandidateKey(candidateEntity.getCandidateKey());
    verify(resumeTechStackRepository, times(1)).findAllByResumeKey(resumeEntity.getResumeKey());
    assertEquals(1, result.size());
    assertEquals("candidateKey", result.get(0).getCandidateKey());
    assertEquals("name", result.get(0).getCandidateName());
    assertEquals("resumeKey", result.get(0).getResumeKey());
  }

  @Test
  @DisplayName("해당 채용 단계의 지원자 리스트 조회 - 실패 : JOB_POSTING_NOT_FOUND 예외 발생")
  void testGetCandidatesListByStepId_JobPostingNotFound() {
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    User user = new User("email", "password", new ArrayList<>());

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.empty());

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    CustomException exception = assertThrows(CustomException.class, () -> {
      jobPostingStepService.getCandidatesListByStepId(jobPostingKey, stepId);
    });

    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    assertEquals(JOB_POSTING_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("해당 채용 단계의 지원자 리스트 조회 - 실패 : JOB_POSTING_STEP_NOT_FOUND 예외 발생")
  void testGetCandidatesListByStepId_StepNotFound() {
    String jobPostingKey = "jobPostingKey";
    Long stepId = 1L;
    User user = new User("email", "password", new ArrayList<>());
    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey("companyKey")
        .build();
    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey("companyKey")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(companyEntity));
    when(jobPostingStepRepository.existsByIdAndJobPostingKey(stepId, jobPostingKey)).thenReturn(
        false);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);

    CustomException exception = assertThrows(CustomException.class, () -> {
      jobPostingStepService.getCandidatesListByStepId(jobPostingKey, stepId);
    });

    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
    verify(companyRepository, times(1)).findByEmail(user.getUsername());
    verify(jobPostingStepRepository, times(1)).existsByIdAndJobPostingKey(stepId, jobPostingKey);
    assertEquals(JOB_POSTING_STEP_NOT_FOUND, exception.getErrorCode());
  }
}
