package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.KeyGenerator;
import com.ctrls.auto_enter_view.dto.resume.CareerDto;
import com.ctrls.auto_enter_view.dto.resume.CertificateDto;
import com.ctrls.auto_enter_view.dto.resume.ExperienceDto;
import com.ctrls.auto_enter_view.dto.resume.ResumeDto.Request;
import com.ctrls.auto_enter_view.dto.resume.ResumeReadDto.Response;
import com.ctrls.auto_enter_view.entity.ApplicantEntity;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.entity.ResumeCareerEntity;
import com.ctrls.auto_enter_view.entity.ResumeCertificateEntity;
import com.ctrls.auto_enter_view.entity.ResumeEntity;
import com.ctrls.auto_enter_view.entity.ResumeExperienceEntity;
import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.TechStack;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.ApplicantRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.ResumeCareerRepository;
import com.ctrls.auto_enter_view.repository.ResumeCertificateRepository;
import com.ctrls.auto_enter_view.repository.ResumeExperienceRepository;
import com.ctrls.auto_enter_view.repository.ResumeImageRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.repository.ResumeTechStackRepository;
import java.time.LocalDate;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

  @Mock
  private KeyGenerator keyGenerator;

  @Mock
  private ApplicantRepository applicantRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private ResumeCareerRepository resumeCareerRepository;

  @Mock
  private ResumeCertificateRepository resumeCertificateRepository;

  @Mock
  private ResumeExperienceRepository resumeExperienceRepository;

  @Mock
  private ResumeRepository resumeRepository;

  @Mock
  private ResumeImageRepository resumeImageRepository;

  @Mock
  private ResumeTechStackRepository resumeTechStackRepository;

  @Captor
  ArgumentCaptor<ResumeEntity> resumeCaptor;

  @Captor
  ArgumentCaptor<List<ResumeCareerEntity>> resumeCareerCaptor;

  @Captor
  ArgumentCaptor<List<ResumeExperienceEntity>> resumeExperienceCaptor;

  @Captor
  ArgumentCaptor<List<ResumeTechStackEntity>> resumeTechStackCaptor;

  @Captor
  ArgumentCaptor<List<ResumeCertificateEntity>> resumeCertificateCaptor;

  @InjectMocks
  private ResumeService resumeService;

  private final UserDetails candidateDetails = new User("candidate@naver.com", "testPassword",
      List.of(new SimpleGrantedAuthority(UserRole.ROLE_CANDIDATE.name())));

  private final UserDetails companyDetails = new User("company@naver.com", "testPassword",
      List.of(new SimpleGrantedAuthority(UserRole.ROLE_COMPANY.name())));

  @Test
  @DisplayName("이력서 생성_성공")
  void createResume_Success() {
    // given
    String title = "이력서";
    String candidateKey = "candidateKey";
    String resumeKey = "resumeKey";

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .build();

    Request request = Request.builder()
        .title(title)
        .career(List.of(CareerDto.Request.builder()
            .startDate(LocalDate.now())
            .endDate(LocalDate.now())
            .build()))
        .experience(List.of(ExperienceDto.builder().build()))
        .techStack(List.of(TechStack.ORACLE))
        .certificates(List.of(CertificateDto.builder().build()))
        .build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.of(candidateEntity));
    when(keyGenerator.generateKey()).thenReturn(resumeKey);

    // execute
    resumeService.createResume(candidateDetails, candidateKey, request);

    // then
    verify(resumeRepository, times(1)).save(resumeCaptor.capture());
    verify(resumeCareerRepository, times(1)).saveAll(resumeCareerCaptor.capture());
    verify(resumeExperienceRepository, times(1)).saveAll(resumeExperienceCaptor.capture());
    verify(resumeTechStackRepository, times(1)).saveAll(resumeTechStackCaptor.capture());
    verify(resumeCertificateRepository, times(1)).saveAll(resumeCertificateCaptor.capture());

    assertEquals(candidateKey, resumeCaptor.getValue().getCandidateKey());
    assertEquals(resumeKey, resumeCareerCaptor.getValue().get(0).getResumeKey());
    assertEquals(resumeKey, resumeExperienceCaptor.getValue().get(0).getResumeKey());
    assertEquals(resumeKey, resumeTechStackCaptor.getValue().get(0).getResumeKey());
    assertEquals(resumeKey, resumeCertificateCaptor.getValue().get(0).getResumeKey());
  }

  @Test
  @DisplayName("이력서 생성_실패_CandidateNotFound")
  void createResume_Fail_CandidateNotFound() {
    // given
    String candidateKey = "candidateKey";
    Request request = Request.builder().build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.empty());

    // then
    CustomException exception = assertThrows(CustomException.class,
        // execute
        () -> resumeService.createResume(candidateDetails, candidateKey, request));

    assertEquals(ErrorCode.CANDIDATE_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("이력서 생성_실패_NoAuthority")
  void createResume_Fail_NoAuthority() {
    // given
    String candidateKey = "candidateKey";
    String wrongCandidateKey = "wrongCandidateKey";
    Request request = Request.builder().build();

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.of(candidateEntity));

    // then
    CustomException exception = assertThrows(CustomException.class,
        // execute
        () -> resumeService.createResume(candidateDetails, wrongCandidateKey, request));

    verify(resumeRepository, times(0)).save(any());
    verify(resumeCareerRepository, times(0)).saveAll(anyList());
    verify(resumeExperienceRepository, times(0)).saveAll(anyList());
    verify(resumeTechStackRepository, times(0)).saveAll(anyList());
    verify(resumeCertificateRepository, times(0)).saveAll(anyList());

    assertEquals(ErrorCode.NO_AUTHORITY, exception.getErrorCode());
  }

  @Test
  @DisplayName("이력서 생성_실패_AlreadyExists")
  void createResume_Fail_AlreadyExists() {
    // given
    String candidateKey = "candidateKey";
    Request request = Request.builder().build();

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.of(candidateEntity));
    when(resumeRepository.existsByCandidateKey(candidateKey)).thenReturn(true);

    // then
    CustomException exception = assertThrows(CustomException.class,
        // execute
        () -> resumeService.createResume(candidateDetails, candidateKey, request));

    verify(resumeRepository, times(0)).save(any());
    verify(resumeCareerRepository, times(0)).saveAll(anyList());
    verify(resumeExperienceRepository, times(0)).saveAll(anyList());
    verify(resumeTechStackRepository, times(0)).saveAll(anyList());
    verify(resumeCertificateRepository, times(0)).saveAll(anyList());

    assertEquals(ErrorCode.ALREADY_EXISTS, exception.getErrorCode());
  }

  @Test
  @DisplayName("이력서 조회_성공_Candidate")
  void readResume_Success_Candidate() {
    // given
    String candidateKey = "candidateKey";

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .build();

    ResumeEntity resumeEntity = ResumeEntity.builder()
        .candidateKey(candidateKey)
        .build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.of(candidateEntity));
    when(resumeRepository.findByCandidateKey(candidateKey)).thenReturn(Optional.of(resumeEntity));

    // execute
    Response response = resumeService.readResume(candidateDetails, candidateKey);

    // then
    assertEquals(candidateKey, response.getCandidateKey());
  }

  @Test
  @DisplayName("이력서 조회_성공_Company")
  void readResume_Success_Company() {
    // given
    String candidateKey = "candidateKey";
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .build();

    ApplicantEntity applicantEntity = ApplicantEntity.builder()
        .jobPostingKey(jobPostingKey)
        .candidateKey(candidateKey)
        .build();

    ResumeEntity resumeEntity = ResumeEntity.builder()
        .candidateKey(candidateKey)
        .build();

    // when
    when(companyRepository.findByEmail(companyDetails.getUsername())).thenReturn(
        Optional.of(companyEntity));
    when(jobPostingRepository.findAllByCompanyKey(companyKey)).thenReturn(
        List.of(jobPostingEntity));
    when(applicantRepository.findAllByJobPostingKey(jobPostingKey)).thenReturn(
        List.of(applicantEntity));
    when(resumeRepository.findByCandidateKey(candidateKey)).thenReturn(Optional.of(resumeEntity));

    // execute
    Response response = resumeService.readResume(companyDetails, candidateKey);

    // then
    assertEquals(candidateKey, response.getCandidateKey());
  }

  @Test
  @DisplayName("이력서 조회_실패_NotApplicant")
  void readResume_Fail_CandidateNotFound() {
    // given
    String candidateKey = "candidateKey";
    String wrongCandidateKey = "wrongCandidateKey";
    String companyKey = "companyKey";
    String jobPostingKey = "jobPostingKey";

    CompanyEntity companyEntity = CompanyEntity.builder()
        .companyKey(companyKey)
        .build();

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .build();

    ApplicantEntity applicantEntity = ApplicantEntity.builder()
        .jobPostingKey(jobPostingKey)
        .candidateKey(wrongCandidateKey)
        .build();

    // when
    when(companyRepository.findByEmail(companyDetails.getUsername())).thenReturn(
        Optional.of(companyEntity));
    when(jobPostingRepository.findAllByCompanyKey(companyKey)).thenReturn(
        List.of(jobPostingEntity));
    when(applicantRepository.findAllByJobPostingKey(jobPostingKey)).thenReturn(
        List.of(applicantEntity));

    // then
    CustomException exception = assertThrows(CustomException.class,
        () -> resumeService.readResume(companyDetails, candidateKey));

    assertEquals(ErrorCode.NO_AUTHORITY, exception.getErrorCode());
  }

  @Test
  @DisplayName("이력서 수정_성공")
  void updateResume_Success() {
    // given
    String title = "title";
    String newTitle = "newTitle";
    String candidateKey = "candidateKey";

    Request request = Request.builder()
        .title(newTitle)
        .build();

    ResumeEntity resumeEntity = ResumeEntity.builder()
        .title(title)
        .candidateKey(candidateKey)
        .build();

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.of(candidateEntity));
    when(resumeRepository.findByCandidateKey(candidateKey)).thenReturn(Optional.of(resumeEntity));

    // execute
    resumeService.updateResume(candidateDetails, candidateKey, request);

    // then
    assertEquals(newTitle, resumeEntity.getTitle());
  }

  @Test
  @DisplayName("이력서 수정_실패_NoAuthority")
  void updateResume_Fail_NoAuthority() {
    // given
    String candidateKey = "candidateKey";
    String wrongCandidateKey = "wrongCandidateKey";

    Request request = Request.builder().build();

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.of(candidateEntity));

    // then
    CustomException exception = assertThrows(CustomException.class,
        // execute
        () -> resumeService.updateResume(candidateDetails, wrongCandidateKey, request));

    verify(resumeRepository, times(0)).findByCandidateKey(wrongCandidateKey);

    assertEquals(ErrorCode.NO_AUTHORITY, exception.getErrorCode());
  }

  @Test
  @DisplayName("이력서 삭제_성공")
  void deleteResume_Success() {
    // given
    String candidateKey = "candidateKey";
    String resumeKey = "resumeKey";

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .build();

    ResumeEntity resumeEntity = ResumeEntity.builder()
        .resumeKey(resumeKey)
        .candidateKey(candidateKey)
        .build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.of(candidateEntity));
    when(resumeRepository.findByCandidateKey(candidateKey)).thenReturn(Optional.of(resumeEntity));

    // then
    resumeService.deleteResume(candidateDetails, candidateKey);

    verify(resumeRepository, times(1)).deleteByCandidateKey(candidateKey);
    verify(resumeTechStackRepository, times(1)).deleteAllByResumeKey(resumeKey);
    verify(resumeCareerRepository, times(1)).deleteAllByResumeKey(resumeKey);
    verify(resumeExperienceRepository, times(1)).deleteAllByResumeKey(resumeKey);
    verify(resumeCertificateRepository, times(1)).deleteAllByResumeKey(resumeKey);
  }

  @Test
  @DisplayName("이력서 삭제_실패_ResumeNotFound")
  void deleteResume_Fail_ResumeNotFound() {
    // given
    String candidateKey = "candidateKey";

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .build();

    // when
    when(candidateRepository.findByEmail(candidateDetails.getUsername())).thenReturn(
        Optional.of(candidateEntity));
    when(resumeRepository.findByCandidateKey(candidateKey)).thenReturn(Optional.empty());

    // then
    CustomException exception = assertThrows(CustomException.class,
        // execute
        () -> resumeService.deleteResume(candidateDetails, candidateKey));

    assertEquals(ErrorCode.RESUME_NOT_FOUND, exception.getErrorCode());
  }
}