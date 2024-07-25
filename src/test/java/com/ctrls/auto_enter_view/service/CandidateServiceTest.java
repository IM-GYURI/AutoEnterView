package com.ctrls.auto_enter_view.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto;
import com.ctrls.auto_enter_view.entity.AppliedJobPostingEntity;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.AppliedJobPostingRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
public class CandidateServiceTest {

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private ResumeRepository resumeRepository;

  @Mock
  private AppliedJobPostingRepository appliedJobPostingRepository;

  @Mock
  private KeyGenerator keyGenerator;

  @InjectMocks
  private CandidateService candidateService;

  @BeforeEach
  void setup() {

    User user = new User("email@example.com", "password", new ArrayList<>());
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
  }


  @Test
  @DisplayName("이력서 존재 여부 확인 - 이력서가 존재하는 경우")
  void hasResumeWhenResumeExists() {
    // given
    String mockedCompanyKey = "companyKey";
    when(keyGenerator.generateKey()).thenReturn(mockedCompanyKey);
    String companyKey = keyGenerator.generateKey();

    when(resumeRepository.existsByCandidateKey(companyKey)).thenReturn(true);

    // when
    boolean result = candidateService.hasResume(companyKey);

    // then
    assertTrue(result);
    verify(resumeRepository, times(1)).existsByCandidateKey(companyKey);
  }

  @Test
  @DisplayName("이력서 존재 여부 확인 - 이력서가 존재하지 않는 경우")
  void hasResumeWhenResumeDoesNotExist() {
    // given
    String mockedCandidateKey = "candidateKey";
    when(keyGenerator.generateKey()).thenReturn(mockedCandidateKey);
    String candidateKey = keyGenerator.generateKey();

    when(resumeRepository.existsByCandidateKey(candidateKey)).thenReturn(false);

    // when
    boolean result = candidateService.hasResume(candidateKey);

    // then
    assertFalse(result);
    verify(resumeRepository, times(1)).existsByCandidateKey(candidateKey);
  }

  @Test
  @DisplayName("지원자가 지원한 채용 공고 조회 테스트 - 성공")
  void getApplyJobPostingsSuccessTest() {
    // given
    String email = "test@example.com";
    String mockedCandidateKey = "candidateKey";
    when(keyGenerator.generateKey()).thenReturn(mockedCandidateKey);
    String candidateKey = keyGenerator.generateKey();
    int page = 1;
    int size = 20;
    Pageable pageable = PageRequest.of(page - 1, size);

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey(candidateKey)
        .email(email)
        .build();

    AppliedJobPostingEntity appliedJobPostingEntity1 = AppliedJobPostingEntity.builder()
        .candidateKey(candidateKey)
        .jobPostingKey("jobPostingKey1")
        .build();

    AppliedJobPostingEntity appliedJobPostingEntity2 = AppliedJobPostingEntity.builder()
        .candidateKey(candidateKey)
        .jobPostingKey("jobPostingKey2")
        .build();

    List<AppliedJobPostingEntity> appliedJobPostingEntities = Arrays.asList(
        appliedJobPostingEntity1, appliedJobPostingEntity2);
    Page<AppliedJobPostingEntity> appliedJobPostingPage = new PageImpl<>(
        appliedJobPostingEntities, pageable, appliedJobPostingEntities.size());

    when(candidateRepository.findByEmail(email)).thenReturn(Optional.of(candidateEntity));
    when(appliedJobPostingRepository.findAllByCandidateKey(candidateKey, pageable))
        .thenReturn(appliedJobPostingPage);

    // when
    CandidateApplyDto.Response response = candidateService.getApplyJobPostings(
        new User(email, "", Collections.emptyList()), candidateKey, page, size);

    // then
    assertEquals(2, response.getAppliedJobPostingsList().size());
    assertEquals(1, response.getTotalPages());
    assertEquals(2, response.getTotalElements());
  }

  @Test
  @DisplayName("지원자가 지원한 채용 공고 조회 테스트 - 실패 : 가입된 지원자를 찾을 수 없음")
  void getApplyJobPostingsFailCandidateNotFoundTest() {
    // given
    String email = "test@example.com";
    String mockedCandidateKey = "candidateKey";
    when(keyGenerator.generateKey()).thenReturn(mockedCandidateKey);
    String candidateKey = keyGenerator.generateKey();
    int page = 1;
    int size = 20;

    when(candidateRepository.findByEmail(email)).thenReturn(Optional.empty());

    // when
    Throwable throwable = catchThrowable(() -> candidateService.getApplyJobPostings(
        new User(email, "", Collections.emptyList()), candidateKey, page, size));

    // then
    assertThat(throwable).isInstanceOf(CustomException.class);
    assertThat(((CustomException) throwable).getErrorCode()).isEqualTo(
        ErrorCode.CANDIDATE_NOT_FOUND);
  }

  @Test
  @DisplayName("지원자가 지원한 채용 공고 조회 테스트 - 실패 : 권한 없음")
  void getApplyJobPostingsFailNoAuthorityTest() {
    // given
    String email = "test@example.com";
    String mockedCandidateKey = "candidateKey";
    when(keyGenerator.generateKey()).thenReturn(mockedCandidateKey);
    String candidateKey = keyGenerator.generateKey();
    int page = 1;
    int size = 20;

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .candidateKey("anotherCandidateKey")
        .email(email)
        .build();

    when(candidateRepository.findByEmail(email)).thenReturn(Optional.of(candidateEntity));

    // when
    Throwable throwable = catchThrowable(() -> candidateService.getApplyJobPostings(
        new User(email, "", Collections.emptyList()), candidateKey, page, size));

    // then
    assertThat(throwable).isInstanceOf(CustomException.class);
    assertThat(((CustomException) throwable).getErrorCode()).isEqualTo(ErrorCode.NO_AUTHORITY);
  }
}