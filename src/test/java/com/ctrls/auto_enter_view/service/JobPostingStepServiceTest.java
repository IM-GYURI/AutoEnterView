package com.ctrls.auto_enter_view.service;

import static com.ctrls.auto_enter_view.enums.ErrorCode.JOB_POSTING_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.RESUME_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.ErrorCode.USER_NOT_FOUND;
import static com.ctrls.auto_enter_view.enums.TechStack.JAVA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  @DisplayName("채용 공고 단계별 지원자 리스트 조회 : JOB_POSTING_NOT_FOUND")
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
}
