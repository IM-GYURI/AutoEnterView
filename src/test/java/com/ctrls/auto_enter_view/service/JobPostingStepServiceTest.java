package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.RESUME_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.TechStack.JAVA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.KeyGenerator;
import com.ctrls.auto_enter_view.dto.candidateList.CandidateTechStackInterviewInfoDto;
import com.ctrls.auto_enter_view.dto.jobPosting.JobPostingEveryInfoDto;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleEntity;
import com.ctrls.auto_enter_view.entity.InterviewScheduleParticipantsEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.JobPostingStepEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleParticipantsRepository;
import com.ctrls.auto_enter_view.repository.InterviewScheduleRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.JobPostingStepRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
  private ResumeTechStackRepository resumeTechStackRepository;

  @Mock
  private ResumeRepository resumeRepository;

  @Mock
  private InterviewScheduleRepository interviewScheduleRepository;

  @Mock
  private InterviewScheduleParticipantsRepository interviewScheduleParticipantsRepository;

  @Mock
  private KeyGenerator keyGenerator;

  @InjectMocks
  private JobPostingStepService jobPostingStepService;

  @Test
  @DisplayName("채용 공고 단계별 지원자 리스트 조회 : 성공")
  void getCandidatesListByStepId_Success() {
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";
    Long stepId = 1L;
    String candidateKey = "candidateKey";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("test@example.com")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(stepId)
        .jobPostingKey(jobPostingKey)
        .step("면접 단계")
        .build();

    CandidateListEntity candidateListEntity = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .candidateName("John Doe")
        .build();

    ResumeEntity resumeEntity = ResumeEntity.builder()
        .resumeKey("resumeKey")
        .build();

    ResumeTechStackEntity resumeTechStackEntity = ResumeTechStackEntity.builder()
        .id(1L)
        .techStackName(JAVA)
        .build();

    InterviewScheduleEntity interviewScheduleEntity = InterviewScheduleEntity.builder()
        .jobPostingStepId(1L)
        .interviewScheduleKey("interviewScheduleKey")
        .firstInterviewDate(LocalDate.parse("2024-04-04"))
        .lastInterviewDate(LocalDate.parse("2024-04-05"))
        .build();

    InterviewScheduleParticipantsEntity interviewScheduleParticipantsEntity = InterviewScheduleParticipantsEntity.builder()
        .interviewStartDatetime(LocalDateTime.now())
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(userDetails.getUsername()))
        .thenReturn(Optional.of(companyEntity));
    when(jobPostingStepRepository.findAllByJobPostingKey(jobPostingKey))
        .thenReturn(Collections.singletonList(jobPostingStepEntity));
    when(candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Collections.singletonList(candidateListEntity));
    when(resumeRepository.findByCandidateKey(candidateKey))
        .thenReturn(Optional.of(resumeEntity));
    when(resumeTechStackRepository.findAllByResumeKey("resumeKey"))
        .thenReturn(Collections.singletonList(resumeTechStackEntity));
    when(interviewScheduleRepository.findByJobPostingStepId(stepId))
        .thenReturn(Optional.of(interviewScheduleEntity));
    when(interviewScheduleParticipantsRepository.findByJobPostingStepIdAndCandidateKey(stepId,
        candidateKey))
        .thenReturn(Optional.of(interviewScheduleParticipantsEntity));

    List<JobPostingEveryInfoDto> result = jobPostingStepService.getCandidatesListByStepId(
        userDetails, jobPostingKey);

    assertNotNull(result);
    assertEquals(1, result.size());

    JobPostingEveryInfoDto dto = result.get(0);

    assertEquals(stepId, dto.getStepId());
    assertEquals("면접 단계", dto.getStepName());
    assertEquals(1, dto.getCandidateTechStackInterviewInfoDtoList().size());

    CandidateTechStackInterviewInfoDto infoDto = dto.getCandidateTechStackInterviewInfoDtoList()
        .get(0);

    assertEquals(candidateKey, infoDto.getCandidateKey());
    assertEquals("John Doe", infoDto.getCandidateName());
    assertEquals("resumeKey", infoDto.getResumeKey());
    assertNotNull(infoDto.getTechStack());
    assertEquals(JAVA, infoDto.getTechStack().get(0));
    assertNotNull(infoDto.getScheduleDateTime());
  }

  @Test
  @DisplayName("채용 공고 단계별 지원자 리스트 조회 : 실패 - USER_NOT_FOUND")
  void getCandidatesListByStepId_UserNotFoundFailure() {
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(userDetails.getUsername()))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      jobPostingStepService.getCandidatesListByStepId(userDetails, jobPostingKey);
    });

    assertEquals(USER_NOT_FOUND, thrownException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 단계별 지원자 리스트 조회 : 실패 - JOB_POSTING_NOT_FOUND")
  void getCandidatesListByStepId_JobPostingNotFoundFailure() {
    UserDetails userDetails = mock(UserDetails.class);

    when(jobPostingRepository.findByJobPostingKey("jobPostingKey"))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      jobPostingStepService.getCandidatesListByStepId(userDetails, "jobPostingKey");
    });

    assertEquals(JOB_POSTING_NOT_FOUND, thrownException.getErrorCode());
  }

  @Test
  @DisplayName("채용 공고 단계별 지원자 리스트 조회 : 실패 - RESUME_NOT_FOUND")
  void getCandidatesListByStepId_ResumeNotFoundFailure() {
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";
    Long stepId = 1L;
    String candidateKey = "candidateKey";

    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .build();

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .email("test@example.com")
        .build();

    JobPostingStepEntity jobPostingStepEntity = JobPostingStepEntity.builder()
        .id(stepId)
        .jobPostingKey(jobPostingKey)
        .step("면접 단계")
        .build();

    CandidateListEntity candidateListEntity = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .candidateName("John Doe")
        .build();

    when(jobPostingRepository.findByJobPostingKey(jobPostingKey))
        .thenReturn(Optional.of(jobPostingEntity));
    when(companyRepository.findByEmail(userDetails.getUsername()))
        .thenReturn(Optional.of(companyEntity));
    when(jobPostingStepRepository.findAllByJobPostingKey(jobPostingKey))
        .thenReturn(Collections.singletonList(jobPostingStepEntity));
    when(candidateListRepository.findAllByJobPostingKeyAndJobPostingStepId(jobPostingKey, stepId))
        .thenReturn(Collections.singletonList(candidateListEntity));
    when(resumeRepository.findByCandidateKey(candidateKey))
        .thenReturn(Optional.empty());

    CustomException thrownException = assertThrows(CustomException.class, () -> {
      jobPostingStepService.getCandidatesListByStepId(userDetails, jobPostingKey);
    });

    assertEquals(RESUME_NOT_FOUND, thrownException.getErrorCode());
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
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));

    Long nextStepId = currentStepId + 1;
    JobPostingStepEntity nextStepEntity = JobPostingStepEntity.builder()
        .id(nextStepId)
        .jobPostingKey(jobPostingKey)
        .build();
    when(jobPostingStepRepository.findByJobPostingKeyAndId(jobPostingKey, nextStepId)).thenReturn(
        Optional.of(nextStepEntity));

    List<CandidateListEntity> candidateListEntities = new ArrayList<>();
    for (String candidateKey : candidateKeys) {
      CandidateListEntity candidateListEntity = CandidateListEntity.builder()
          .candidateKey(candidateKey)
          .jobPostingKey(jobPostingKey)
          .jobPostingStepId(currentStepId)
          .build();
      candidateListEntities.add(candidateListEntity);
    }
    when(candidateListRepository.findAllByCandidateKeyInAndJobPostingKey(candidateKeys,
        jobPostingKey)).thenReturn(candidateListEntities);

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
  @DisplayName("채용 단계 올리기 테스트 - 실패 : 지원자의 현재 단계가 일치하지 않는 경우")
  void editStepIdInvalidCurrentStepIdTest() {
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

    List<CandidateListEntity> candidateListEntities = new ArrayList<>();
    for (String candidateKey : candidateKeys) {
      CandidateListEntity candidateListEntity = CandidateListEntity.builder()
          .candidateKey(candidateKey)
          .jobPostingKey(jobPostingKey)
          .jobPostingStepId(currentStepId + 1)
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
    CustomException exception = assertThrows(CustomException.class, () ->
        jobPostingStepService.editStepId(currentStepId, candidateKeys, jobPostingKey, userDetails)
    );

    // then
    assertEquals(ErrorCode.INVALID_CURRENT_STEP_ID, exception.getErrorCode());
    verify(companyRepository).findByEmail(companyEmail);
    verify(jobPostingRepository).findByJobPostingKey(jobPostingKey);
    verify(candidateListRepository).findAllByCandidateKeyInAndJobPostingKey(candidateKeys, jobPostingKey);
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
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));

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
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));

    Long nextStepId = currentStepId + 1;
    when(jobPostingStepRepository.findByJobPostingKeyAndId(jobPostingKey, nextStepId)).thenReturn(
        Optional.empty());

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
    when(jobPostingRepository.findByJobPostingKey(jobPostingKey)).thenReturn(
        Optional.of(jobPostingEntity));

    Long nextStepId = currentStepId + 1;
    JobPostingStepEntity nextStepEntity = JobPostingStepEntity.builder()
        .id(nextStepId)
        .jobPostingKey(jobPostingKey)
        .build();
    when(jobPostingStepRepository.findByJobPostingKeyAndId(jobPostingKey, nextStepId)).thenReturn(
        Optional.of(nextStepEntity));

    // 지원자를 찾을 수 없는 상황 설정
    List<CandidateListEntity> candidateListEntities = List.of(CandidateListEntity.builder()
        .candidateKey("candidateKey1")
        .jobPostingKey(jobPostingKey)
        .build());
    when(candidateListRepository.findAllByCandidateKeyInAndJobPostingKey(candidateKeys,
        jobPostingKey))
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
    verify(candidateListRepository).findAllByCandidateKeyInAndJobPostingKey(candidateKeys,
        jobPostingKey);
  }
}
