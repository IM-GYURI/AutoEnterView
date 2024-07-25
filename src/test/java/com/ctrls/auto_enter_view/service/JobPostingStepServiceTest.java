package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.KeyGenerator;
import com.ctrls.auto_enter_view.dto.candidateList.CandidateTechStackInterviewInfoDto;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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

  @Mock
  private KeyGenerator keyGenerator;

  @InjectMocks
  private JobPostingStepService jobPostingStepService;

//  @Test
//  @DisplayName("해당 채용 단계의 지원자 리스트 조회 - 성공")
//  void testGetCandidatesListByStepId_Success() {
//    String jobPostingKey = "jobPostingKey";
//    Long stepId = 1L;
//    User user = new User("email", "password", new ArrayList<>());
//
//    CompanyEntity companyEntity = CompanyEntity.builder()
//        .companyKey("companyKey")
//        .build();
//    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
//        .companyKey("companyKey")
//        .build();
//    CandidateListEntity candidateEntity = CandidateListEntity.builder()
//        .candidateKey("candidateKey")
//        .candidateName("name")
//        .build();
//    ResumeEntity resumeEntity = ResumeEntity.builder()
//        .resumeKey("resumeKey")
//        .build();
//    ResumeTechStackEntity techStackEntity = ResumeTechStackEntity.builder()
//        .techStackName(TechStack.JAVA)
//        .build();
//
//    List<CandidateListEntity> candidateList = List.of(candidateEntity);
//    List<ResumeTechStackEntity> techStackList = List.of(techStackEntity);
//
//    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
//        Optional.of(jobPostingEntity));
//    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(companyEntity));
//    when(candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(jobPostingKey,
//        stepId)).thenReturn(candidateList);
//    when(resumeRepository.findByCandidateKey(candidateEntity.getCandidateKey())).thenReturn(
//        Optional.of(resumeEntity));
//    when(resumeTechStackRepository.findAllByResumeKey(resumeEntity.getResumeKey())).thenReturn(
//        techStackList);
//    when(jobPostingStepRepository.existsByIdAndJobPostingKey(stepId, jobPostingKey)).thenReturn(
//        true);
//
//    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//    securityContext.setAuthentication(
//        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
//    SecurityContextHolder.setContext(securityContext);
//
//    List<CandidateTechStackInterviewInfoDto> result = jobPostingStepService.getCandidatesListByStepId(
//        jobPostingKey, stepId);
//
//    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
//    verify(companyRepository, times(1)).findByEmail(user.getUsername());
//    verify(candidateListRepository, times(1)).findAllByJobPostingKeyAndJobPostingStepId(
//        jobPostingKey, stepId);
//    verify(resumeRepository, times(1)).findByCandidateKey(candidateEntity.getCandidateKey());
//    verify(resumeTechStackRepository, times(1)).findAllByResumeKey(resumeEntity.getResumeKey());
//    assertEquals(1, result.size());
//    assertEquals("candidateKey", result.get(0).getCandidateKey());
//    assertEquals("name", result.get(0).getCandidateName());
//    assertEquals("resumeKey", result.get(0).getResumeKey());
//    assertEquals(List.of(TechStack.JAVA), result.get(0).getTechStack());
//  }
//
//  @Test
//  @DisplayName("해당 채용 단계의 지원자 리스트 조회 - 실패 : JOB_POSTING_NOT_FOUND 예외 발생")
//  void testGetCandidatesListByStepId_JobPostingNotFound() {
//    String jobPostingKey = "jobPostingKey";
//    Long stepId = 1L;
//    User user = new User("email", "password", new ArrayList<>());
//
//    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.empty());
//
//    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//    securityContext.setAuthentication(
//        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
//    SecurityContextHolder.setContext(securityContext);
//
//    CustomException exception = assertThrows(CustomException.class, () -> {
//      jobPostingStepService.getCandidatesListByStepId(jobPostingKey, stepId);
//    });
//
//    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
//    assertEquals(JOB_POSTING_NOT_FOUND, exception.getErrorCode());
//  }
//
//  @Test
//  @DisplayName("해당 채용 단계의 지원자 리스트 조회 - 실패 : JOB_POSTING_STEP_NOT_FOUND 예외 발생")
//  void testGetCandidatesListByStepId_StepNotFound() {
//    String jobPostingKey = "jobPostingKey";
//    Long stepId = 1L;
//    User user = new User("email", "password", new ArrayList<>());
//    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
//        .companyKey("companyKey")
//        .build();
//    CompanyEntity companyEntity = CompanyEntity.builder()
//        .companyKey("companyKey")
//        .build();
//
//    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
//        Optional.of(jobPostingEntity));
//    when(companyRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(companyEntity));
//    when(jobPostingStepRepository.existsByIdAndJobPostingKey(stepId, jobPostingKey)).thenReturn(
//        false);
//
//    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//    securityContext.setAuthentication(
//        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
//    SecurityContextHolder.setContext(securityContext);
//
//    CustomException exception = assertThrows(CustomException.class, () -> {
//      jobPostingStepService.getCandidatesListByStepId(jobPostingKey, stepId);
//    });
//
//    verify(jobPostingRepository, times(1)).findByJobPostingKey(jobPostingKey);
//    verify(companyRepository, times(1)).findByEmail(user.getUsername());
//    verify(jobPostingStepRepository, times(1)).existsByIdAndJobPostingKey(stepId, jobPostingKey);
//    assertEquals(JOB_POSTING_STEP_NOT_FOUND, exception.getErrorCode());
//  }

  @Test
  @DisplayName("채용 공고 키로 채용 단계 조회 테스트")
  void getStepByJobPostingKeyTest() {
    // given
    String jobPostingKey = "jobPostingKey";
    List<JobPostingStepEntity> entities = new ArrayList<>();
    entities.add(new JobPostingStepEntity(1L, jobPostingKey, "서류 전형"));
    entities.add(new JobPostingStepEntity(2L, jobPostingKey, "1차 면접"));
    entities.add(new JobPostingStepEntity(3L, jobPostingKey, "2차 면접"));

    when(jobPostingStepRepository.findByJobPostingKey(jobPostingKey)).thenReturn(entities);

    // when
    List<String> steps = jobPostingStepService.getStepByJobPostingKey(jobPostingKey);

    // then
    assertEquals(3, steps.size());
    assertEquals("서류 전형", steps.get(0));
    assertEquals("1차 면접", steps.get(1));
    assertEquals("2차 면접", steps.get(2));
  }

  @Test
  @DisplayName("채용 단계 올리기 테스트 - 성공")
  void editStepIdSuccessTest() {
    // given
    long currentStepId = 1L;
    List<String> candidateKeys = List.of("candidateKey1", "candidateKey2");
    String jobPostingKey = "jobPostingKey";
    String companyEmail = "company@example.com";
    String mockedCompanyKey = "companyKey";

    // KeyGenerator mock 설정
    when(keyGenerator.generateKey()).thenReturn(mockedCompanyKey);
    String companyKey = keyGenerator.generateKey();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(companyEmail)
        .build();
    when(companyRepository.findByEmail(companyEmail)).thenReturn(Optional.of(companyEntity));

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey(companyKey)
        .jobPostingKey(jobPostingKey)
        .build();
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(jobPostingEntity));

    Long nextStepId = currentStepId + 1;
    JobPostingStepEntity nextStepEntity = JobPostingStepEntity.builder()
        .id(nextStepId)
        .jobPostingKey(jobPostingKey)
        .build();
    when(jobPostingStepRepository.findByJobPostingKeyAndId(jobPostingKey, nextStepId)).thenReturn(Optional.of(nextStepEntity));

    List<CandidateListEntity> candidateListEntities = new ArrayList<>();
    for (String candidateKey : candidateKeys) {
      CandidateListEntity candidateListEntity = CandidateListEntity.builder()
          .candidateKey(candidateKey)
          .jobPostingKey(jobPostingKey)
          .build();
      candidateListEntities.add(candidateListEntity);
    }
    when(candidateListRepository.findAllByCandidateKeyInAndJobPostingKey(candidateKeys, jobPostingKey)).thenReturn(candidateListEntities);

    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
        .username(companyEmail)
        .password("")
        .roles("COMPANY")
        .build();

    // when
    jobPostingStepService.editStepId(currentStepId, candidateKeys, jobPostingKey, userDetails);

    // then
    for (CandidateListEntity candidate : candidateListEntities) {
      assertEquals(nextStepId, candidate.getJobPostingStepId());
    }
  }

  @Test
  @DisplayName("채용 단계 올리기 테스트 - 실패 : 회사를 찾을 수 없는 경우")
  void editStepIdCompanyNotFoundTest() {
    // given
    long currentStepId = 1L;
    List<String> candidateKeys = List.of("candidateKey1", "candidateKey2");
    String jobPostingKey = "jobPostingKey";
    String companyEmail = "nonexistent@example.com";

    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
        .username(companyEmail)
        .password("")
        .roles("COMPANY")
        .build();

    when(companyRepository.findByEmail(companyEmail)).thenReturn(Optional.empty());

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingStepService.editStepId(currentStepId, candidateKeys, jobPostingKey, userDetails)
    );

    // then
    assertEquals(ErrorCode.COMPANY_NOT_FOUND, exception.getErrorCode());
    verify(companyRepository).findByEmail(companyEmail);
  }

  @Test
  @DisplayName("채용 단계 올리기 테스트 - 실패 : 채용 공고를 찾을 수 없는 경우")
  void editStepIdJobPostingNotFoundTest() {
    // given
    long currentStepId = 1L;
    List<String> candidateKeys = List.of("candidateKey1", "candidateKey2");
    String jobPostingKey = "nonexistentJobPosting";
    String companyEmail = "company@example.com";
    String mockedCompanyKey = "companyKey";

    // KeyGenerator mock 설정
    when(keyGenerator.generateKey()).thenReturn(mockedCompanyKey);
    String companyKey = keyGenerator.generateKey();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(companyEmail)
        .build();
    when(companyRepository.findByEmail(companyEmail)).thenReturn(Optional.of(companyEntity));

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.empty());

    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
        .username(companyEmail)
        .password("")
        .roles("COMPANY")
        .build();

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingStepService.editStepId(currentStepId, candidateKeys, jobPostingKey, userDetails)
    );

    // then
    assertEquals(ErrorCode.JOB_POSTING_NOT_FOUND, exception.getErrorCode());
    verify(companyRepository).findByEmail(companyEmail);
    verify(jobPostingRepository).findByJobPostingKey(jobPostingKey);
  }

  @Test
  @DisplayName("채용 단계 올리기 테스트 - 실패 : 권한이 없는 경우")
  void editStepIdNoAuthorityTest() {
    // given
    long currentStepId = 1L;
    List<String> candidateKeys = List.of("candidateKey1", "candidateKey2");
    String jobPostingKey = "jobPostingKey";
    String companyEmail = "company@example.com";
    String mockedCompanyKey = "companyKey";
    String differentCompanyKey = "differentCompanyKey";

    // KeyGenerator mock 설정
    when(keyGenerator.generateKey()).thenReturn(mockedCompanyKey);
    String companyKey = keyGenerator.generateKey();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(companyEmail)
        .build();
    when(companyRepository.findByEmail(companyEmail)).thenReturn(Optional.of(companyEntity));

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey(differentCompanyKey)  // 다른 회사의 키
        .jobPostingKey(jobPostingKey)
        .build();
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(jobPostingEntity));

    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
        .username(companyEmail)
        .password("")
        .roles("COMPANY")
        .build();

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingStepService.editStepId(currentStepId, candidateKeys, jobPostingKey, userDetails)
    );

    // then
    assertEquals(ErrorCode.NO_AUTHORITY, exception.getErrorCode());
    verify(companyRepository).findByEmail(companyEmail);
    verify(jobPostingRepository).findByJobPostingKey(jobPostingKey);
  }

  @Test
  @DisplayName("채용 단계 올리기 테스트 - 실패 : 다음 단계가 없는 경우")
  void editStepIdNextStepNotFoundTest() {
    // given
    long currentStepId = 1L;
    List<String> candidateKeys = List.of("candidateKey1", "candidateKey2");
    String jobPostingKey = "jobPostingKey";
    String companyEmail = "company@example.com";
    String mockedCompanyKey = "companyKey";

    // KeyGenerator mock 설정
    when(keyGenerator.generateKey()).thenReturn(mockedCompanyKey);
    String companyKey = keyGenerator.generateKey();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(companyEmail)
        .build();
    when(companyRepository.findByEmail(companyEmail)).thenReturn(Optional.of(companyEntity));

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey(companyKey)
        .jobPostingKey(jobPostingKey)
        .build();
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(jobPostingEntity));

    Long nextStepId = currentStepId + 1;
    when(jobPostingStepRepository.findByJobPostingKeyAndId(jobPostingKey, nextStepId)).thenReturn(Optional.empty());

    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
        .username(companyEmail)
        .password("")
        .roles("COMPANY")
        .build();

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingStepService.editStepId(currentStepId, candidateKeys, jobPostingKey, userDetails)
    );

    // then
    assertEquals(ErrorCode.NEXT_STEP_NOT_FOUND, exception.getErrorCode());
    verify(companyRepository).findByEmail(companyEmail);
    verify(jobPostingRepository).findByJobPostingKey(jobPostingKey);
    verify(jobPostingStepRepository).findByJobPostingKeyAndId(jobPostingKey, nextStepId);
  }

  @Test
  @DisplayName("채용 단계 올리기 테스트 - 실패 : 지원자를 찾을 수 없는 경우")
  void editStepIdCandidateNotFoundTest() {
    // given
    long currentStepId = 1L;
    List<String> candidateKeys = List.of("candidateKey1", "candidateKey2");
    String jobPostingKey = "jobPostingKey";
    String companyEmail = "company@example.com";
    String mockedCompanyKey = "companyKey";

    // KeyGenerator mock 설정
    when(keyGenerator.generateKey()).thenReturn(mockedCompanyKey);
    String companyKey = keyGenerator.generateKey();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email(companyEmail)
        .build();
    when(companyRepository.findByEmail(companyEmail)).thenReturn(Optional.of(companyEntity));

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .companyKey(companyKey)
        .jobPostingKey(jobPostingKey)
        .build();
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(Optional.of(jobPostingEntity));

    Long nextStepId = currentStepId + 1;
    JobPostingStepEntity nextStepEntity = JobPostingStepEntity.builder()
        .id(nextStepId)
        .jobPostingKey(jobPostingKey)
        .build();
    when(jobPostingStepRepository.findByJobPostingKeyAndId(jobPostingKey, nextStepId)).thenReturn(Optional.of(nextStepEntity));

    // 지원자를 찾을 수 없는 상황 설정
    List<CandidateListEntity> candidateListEntities = List.of(CandidateListEntity.builder()
        .candidateKey("candidateKey1")
        .jobPostingKey(jobPostingKey)
        .build());
    when(candidateListRepository.findAllByCandidateKeyInAndJobPostingKey(candidateKeys, jobPostingKey))
        .thenReturn(candidateListEntities);

    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
        .username(companyEmail)
        .password("")
        .roles("COMPANY")
        .build();

    // when
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingStepService.editStepId(currentStepId, candidateKeys, jobPostingKey, userDetails)
    );

    // then
    assertEquals(ErrorCode.CANDIDATE_NOT_FOUND, exception.getErrorCode());
    verify(companyRepository).findByEmail(companyEmail);
    verify(jobPostingRepository).findByJobPostingKey(jobPostingKey);
    verify(jobPostingStepRepository).findByJobPostingKeyAndId(jobPostingKey, nextStepId);
    verify(candidateListRepository).findAllByCandidateKeyInAndJobPostingKey(candidateKeys, jobPostingKey);
  }

}
