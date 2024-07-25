package com.ctrls.auto_enter_view.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrls.auto_enter_view.component.KeyGenerator;
import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Request;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Response;
import com.ctrls.auto_enter_view.entity.AppliedJobPostingEntity;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.AppliedJobPostingRepository;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
import com.ctrls.auto_enter_view.repository.ResumeRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class CandidateServiceTest {

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private ResumeRepository resumeRepository;

  @Mock
  private AppliedJobPostingRepository appliedJobPostingRepository;

  @Mock
  private CandidateListRepository candidateListRepository;

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

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

//  @Test
//  @DisplayName("회원가입 성공 테스트")
//  void createCandidateTest() {
//    // DTO 객체 생성
//    SignUpDto.Request request = SignUpDto.Request.builder()
//        .name("name")
//        .email("email@example.com")
//        .password("password")
//        .phoneNumber("010-1111-1111")
//        .build();
//
//    // 인코딩된 비밀번호
//    String encodedPassword = "encodedPassword";
//
//    // 예상 결과 객체 생성
//    SignUpDto.Response expectedResponse = SignUpDto.Response.builder()
//        .email("email@example.com")
//        .name("name")
//        .message("회원가입을 축하드립니다.")
//        .build();
//
//    // 목 설정
//    given(passwordEncoder.encode(request.getPassword())).willReturn(encodedPassword);
//
//    ArgumentCaptor<CandidateEntity> captor = ArgumentCaptor.forClass(CandidateEntity.class);
//
//    // 테스트 실행
//    SignUpDto.Response response = candidateService.signUp(request);
//
//    // 검증
//    verify(candidateRepository, times(1)).save(captor.capture());
//    CandidateEntity capturedCandidate = captor.getValue();
//
//    // 결과 검증
//    assertEquals(request.getName(), capturedCandidate.getName());
//    assertEquals(request.getEmail(), capturedCandidate.getEmail());
//    assertEquals(encodedPassword, capturedCandidate.getPassword());
//    assertEquals(request.getPhoneNumber(), capturedCandidate.getPhoneNumber());
//
//    assertEquals(expectedResponse.getEmail(), response.getEmail());
//    assertEquals(expectedResponse.getName(), response.getName());
//    assertEquals(expectedResponse.getMessage(), response.getMessage());
//  }

//  @Test
//  @DisplayName("회원 탈퇴 성공 테스트")
//  void withdrawSuccessTest() {
//
//    // CandidateEntity 생성
//    CandidateEntity candidate = CandidateEntity.builder()
//        .name("name")
//        .email("email@example.com")
//        .password("encodedPassword")
//        .role(UserRole.ROLE_CANDIDATE)
//        .candidateKey("candidateKey")
//        .phoneNumber("010-1111-1111")
//        .build();
//
//    // 목 설정
//    given(candidateRepository.findByEmail("email@example.com")).willReturn(Optional.of(candidate));
//
//    // 테스트 실행
//    candidateService.withdraw("candidateKey");
//
//    // 검증
//    verify(candidateRepository, times(1)).delete(candidate);
//  }

//  @Test
//  @DisplayName("회원 탈퇴 실패 테스트 - 비밀번호 불일치")
//  void withdrawFailPasswordMismatchTest() {
//
//    // CandidateEntity 생성
//    CandidateEntity candidate = CandidateEntity.builder()
//        .name("name")
//        .email("email@example.com")
//        .password("encodedPassword")
//        .role(UserRole.ROLE_CANDIDATE)
//        .candidateKey("candidateKey")
//        .phoneNumber("010-1111-1111")
//        .build();
//
//    // 목 설정
//    given(candidateRepository.findByEmail("email@example.com")).willReturn(Optional.of(candidate));
//
//    // 테스트 실행 및 검증
//    assertThrows(RuntimeException.class, () -> candidateService.withdraw("candidateKey"));
//  }

//  @Test
//  void findEmail_Success() {
//
//    // given
//    Request request = Request.builder()
//        .name("testName")
//        .phoneNumber("010-0000-0000")
//        .build();
//
//    CandidateEntity candidateEntity = CandidateEntity.builder()
//        .email("test@naver.com")
//        .build();
//
//    // when
//    given(candidateRepository.findByNameAndPhoneNumber(request.getName(),
//        request.getPhoneNumber())).willReturn(Optional.of(candidateEntity));
//
//    // execute
//    Response response = candidateService.findEmail(request);
//
//    // then
//    assertEquals(candidateEntity.getEmail(), response.getEmail());
//  }

//  @Test
//  void findEmail_Failure_EmailNotFound() {
//
//    // given
//    Request request = Request.builder()
//        .name("testName")
//        .phoneNumber("010-0000-0000")
//        .build();
//
//    // when
//    given(candidateRepository.findByNameAndPhoneNumber(request.getName(),
//        request.getPhoneNumber())).willReturn(Optional.empty());
//
//    // then
//    CustomException exception = assertThrows(CustomException.class, () -> {
//      // execute
//      candidateService.findEmail(request);
//    });
//
//    assertEquals(ErrorCode.EMAIL_NOT_FOUND, exception.getErrorCode());
//  }

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