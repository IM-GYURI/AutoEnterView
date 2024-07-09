package com.ctrls.auto_enter_view.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ctrls.auto_enter_view.dto.candidate.CandidateApplyDto;
import com.ctrls.auto_enter_view.dto.candidate.ChangePasswordDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Request;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Response;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.dto.candidate.WithdrawDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.entity.CandidateListEntity;
import com.ctrls.auto_enter_view.entity.CompanyEntity;
import com.ctrls.auto_enter_view.entity.JobPostingEntity;
import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.exception.CustomException;
import com.ctrls.auto_enter_view.repository.CandidateListRepository;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.repository.CompanyRepository;
import com.ctrls.auto_enter_view.repository.JobPostingRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class CandidateServiceTest {

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private CandidateListRepository candidateListRepository;

  @Mock
  private JobPostingRepository jobPostingRepository;

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private CandidateService candidateService;

  @BeforeEach
  void setup() {

    User user = new User("email@example.com", "password", new ArrayList<>());
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
  }

  @Test
  @DisplayName("회원가입 성공 테스트")
  void createCandidateTest() {
    // DTO 객체 생성
    SignUpDto.Request request = SignUpDto.Request.builder()
        .name("name")
        .email("email@example.com")
        .password("password")
        .phoneNumber("010-1111-1111")
        .build();

    // 인코딩된 비밀번호
    String encodedPassword = "encodedPassword";

    // 예상 결과 객체 생성
    SignUpDto.Response expectedResponse = SignUpDto.Response.builder()
        .email("email@example.com")
        .name("name")
        .message("name님 회원가입을 환영합니다")
        .build();

    // 목 설정
    given(passwordEncoder.encode(request.getPassword())).willReturn(encodedPassword);

    ArgumentCaptor<CandidateEntity> captor = ArgumentCaptor.forClass(CandidateEntity.class);

    // 테스트 실행
    SignUpDto.Response response = candidateService.signUp(request);

    // 검증
    verify(candidateRepository, times(1)).save(captor.capture());
    CandidateEntity capturedCandidate = captor.getValue();

    // 결과 검증
    assertEquals(request.getName(), capturedCandidate.getName());
    assertEquals(request.getEmail(), capturedCandidate.getEmail());
    assertEquals(encodedPassword, capturedCandidate.getPassword());
    assertEquals(request.getPhoneNumber(), capturedCandidate.getPhoneNumber());

    assertEquals(expectedResponse.getEmail(), response.getEmail());
    assertEquals(expectedResponse.getName(), response.getName());
    assertEquals(expectedResponse.getMessage(), response.getMessage());
  }

  @Test
  @DisplayName("회원 탈퇴 성공 테스트")
  void withdrawSuccessTest() {
    // DTO 객체 생성
    WithdrawDto.Request request = WithdrawDto.Request.builder()
        .password("password")
        .build();

    // CandidateEntity 생성
    CandidateEntity candidate = CandidateEntity.builder()
        .name("name")
        .email("email@example.com")
        .password("encodedPassword")
        .role(UserRole.ROLE_CANDIDATE)
        .candidateKey("candidateKey")
        .phoneNumber("010-1111-1111")
        .build();

    // 목 설정
    given(candidateRepository.findByEmail("email@example.com")).willReturn(Optional.of(candidate));
    given(passwordEncoder.matches(request.getPassword(), candidate.getPassword())).willReturn(true);

    // 테스트 실행
    candidateService.withdraw(request, "candidateKey");

    // 검증
    verify(candidateRepository, times(1)).delete(candidate);
  }

  @Test
  @DisplayName("회원 탈퇴 실패 테스트 - 비밀번호 불일치")
  void withdrawFailPasswordMismatchTest() {
    // DTO 객체 생성
    WithdrawDto.Request request = WithdrawDto.Request.builder()
        .password("wrongPassword")
        .build();

    // CandidateEntity 생성
    CandidateEntity candidate = CandidateEntity.builder()
        .name("name")
        .email("email@example.com")
        .password("encodedPassword")
        .role(UserRole.ROLE_CANDIDATE)
        .candidateKey("candidateKey")
        .phoneNumber("010-1111-1111")
        .build();

    // 목 설정
    given(candidateRepository.findByEmail("email@example.com")).willReturn(Optional.of(candidate));
    given(passwordEncoder.matches(request.getPassword(), candidate.getPassword())).willReturn(
        false);

    // 테스트 실행 및 검증
    assertThrows(RuntimeException.class, () -> candidateService.withdraw(request, "candidateKey"));
  }

  @Test
  @DisplayName("비밀번호 변경 성공 테스트")
  void changePasswordSuccessTest() {
    // DTO 객체 생성
    ChangePasswordDto.Request request = ChangePasswordDto.Request.builder()
        .oldPassword("oldPassword")
        .newPassword("newPassword")
        .build();

    // CandidateEntity 생성
    CandidateEntity candidate = CandidateEntity.builder()
        .name("name")
        .email("email@example.com")
        .password("encodedOldPassword")
        .role(UserRole.ROLE_CANDIDATE)
        .candidateKey("candidateKey")
        .phoneNumber("010-1111-1111")
        .build();

    // 목 설정
    given(candidateRepository.findByEmail("email@example.com")).willReturn(Optional.of(candidate));
    given(passwordEncoder.matches("oldPassword", candidate.getPassword())).willReturn(true);
    given(passwordEncoder.encode("newPassword")).willReturn("encodedNewPassword");

    // 테스트 실행
    candidateService.changePassword("candidateKey", request);

    // 검증
    verify(candidateRepository, times(1)).save(candidate);
    verify(candidateRepository).save(candidate);
    assertEquals("encodedNewPassword", candidate.getPassword());
  }

  @Test
  @DisplayName("비밀번호 변경 실패 테스트 - 비밀번호 불일치")
  void changePasswordFailPasswordMismatchTest() {
    // DTO 객체 생성
    ChangePasswordDto.Request request = ChangePasswordDto.Request.builder()
        .oldPassword("oldPassword")
        .newPassword("newPassword")
        .build();

    // CandidateEntity 생성
    CandidateEntity candidate = CandidateEntity.builder()
        .name("name")
        .email("email@example.com")
        .password("encodedOldPassword")
        .role(UserRole.ROLE_CANDIDATE)
        .candidateKey("candidateKey")
        .phoneNumber("010-1111-1111")
        .build();

    // 목 설정
    given(candidateRepository.findByEmail("email@example.com")).willReturn(Optional.of(candidate));
    given(passwordEncoder.matches("wrongOldPassword", candidate.getPassword())).willReturn(false);

    // 테스트 실행 및 검증
    assertThrows(RuntimeException.class,
        () -> candidateService.changePassword("candidateKey", request));
  }

  @Test
  void findEmail_Success() {

    // given
    Request request = Request.builder()
        .name("testName")
        .phoneNumber("010-0000-0000")
        .build();

    CandidateEntity candidateEntity = CandidateEntity.builder()
        .email("test@naver.com")
        .build();

    // when
    given(candidateRepository.findByNameAndPhoneNumber(request.getName(),
        request.getPhoneNumber())).willReturn(Optional.of(candidateEntity));

    // execute
    Response response = candidateService.findEmail(request);

    // then
    assertEquals(candidateEntity.getEmail(), response.getEmail());
  }

  @Test
  void findEmail_Failure_EmailNotFound() {

    // given
    Request request = Request.builder()
        .name("testName")
        .phoneNumber("010-0000-0000")
        .build();

    // when
    given(candidateRepository.findByNameAndPhoneNumber(request.getName(),
        request.getPhoneNumber())).willReturn(Optional.empty());

    // then
    CustomException exception = assertThrows(CustomException.class, () -> {
      // execute
      candidateService.findEmail(request);
    });

    assertEquals(ErrorCode.EMAIL_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("지원자가 지원한 채용 공고 조회 테스트 - 성공")
  void getApplyJobPostingsSuccessTest() {
    // given
    String candidateKey = "candidateKey";
    String jobPostingKey1 = "jobPostingKey1";
    String jobPostingKey2 = "jobPostingKey2";
    String companyKey1 = "companyKey1";
    String companyKey2 = "companyKey2";
    String companyName1 = "companyName1";
    String companyName2 = "companyName2";

    CandidateListEntity candidateListEntity1 = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .jobPostingKey(jobPostingKey1)
        .build();

    CandidateListEntity candidateListEntity2 = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .jobPostingKey(jobPostingKey2)
        .build();

    List<CandidateListEntity> candidateListEntities = Arrays.asList(candidateListEntity1, candidateListEntity2);

    JobPostingEntity jobPostingEntity1 = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey1)
        .companyKey(companyKey1)
        .build();

    JobPostingEntity jobPostingEntity2 = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey2)
        .companyKey(companyKey2)
        .build();

    CompanyEntity companyEntity1 = CompanyEntity.builder()
        .companyKey(companyKey1)
        .companyName(companyName1)
        .build();

    CompanyEntity companyEntity2 = CompanyEntity.builder()
        .companyKey(companyKey2)
        .companyName(companyName2)
        .build();

    given(candidateListRepository.findAllByCandidateKey(candidateKey)).willReturn(candidateListEntities);
    given(jobPostingRepository.findByJobPostingKey(jobPostingKey1)).willReturn(Optional.of(jobPostingEntity1));
    given(jobPostingRepository.findByJobPostingKey(jobPostingKey2)).willReturn(Optional.of(jobPostingEntity2));
    given(companyRepository.findByCompanyKey(companyKey1)).willReturn(Optional.of(companyEntity1));
    given(companyRepository.findByCompanyKey(companyKey2)).willReturn(Optional.of(companyEntity2));

    // when
    CandidateApplyDto.Response response = candidateService.getApplyJobPostings(candidateKey);

    // then
    assertEquals(2, response.getApplyJobPostingsList().size());

    CandidateApplyDto.ApplyInfo applyInfo1 = response.getApplyJobPostingsList().get(0);
    assertEquals(jobPostingEntity1.getJobPostingKey(), applyInfo1.getJobPostingKey());
    assertEquals(companyName1, applyInfo1.getCompanyName());

    CandidateApplyDto.ApplyInfo applyInfo2 = response.getApplyJobPostingsList().get(1);
    assertEquals(jobPostingEntity2.getJobPostingKey(), applyInfo2.getJobPostingKey());
    assertEquals(companyName2, applyInfo2.getCompanyName());
  }

  @Test
  @DisplayName("지원자가 지원한 채용 공고 조회 테스트 - 실패 : 채용 공고 찾을 수 없음")
  void getApplyJobPostingsFailJobPostingNotFoundTest() {
    // given
    String candidateKey = "candidateKey";
    String jobPostingKey = "jobPostingKey";

    CandidateListEntity candidateListEntity = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .jobPostingKey(jobPostingKey)
        .build();

    List<CandidateListEntity> candidateListEntities = Collections.singletonList(candidateListEntity);

    given(candidateListRepository.findAllByCandidateKey(candidateKey)).willReturn(candidateListEntities);
    given(jobPostingRepository.findByJobPostingKey(jobPostingKey)).willReturn(Optional.empty());

    // when
    Throwable throwable = catchThrowable(() -> candidateService.getApplyJobPostings(candidateKey));

    // then
    assertThat(throwable).isInstanceOf(CustomException.class);
    assertThat(((CustomException) throwable).getErrorCode()).isEqualTo(ErrorCode.JOB_POSTING_NOT_FOUND);
  }

  @Test
  @DisplayName("지원자가 지원한 채용 공고 조회 테스트 - 실패 : 회사 찾을 수 없음")
  void getApplyJobPostingsFailCompanyNotFoundTest() {
    // given
    String candidateKey = "candidateKey";
    String jobPostingKey = "jobPostingKey";
    String companyKey = "companyKey";

    CandidateListEntity candidateListEntity = CandidateListEntity.builder()
        .candidateKey(candidateKey)
        .jobPostingKey(jobPostingKey)
        .build();

    List<CandidateListEntity> candidateListEntities = Collections.singletonList(candidateListEntity);

    JobPostingEntity jobPostingEntity = JobPostingEntity.builder()
        .jobPostingKey(jobPostingKey)
        .companyKey(companyKey)
        .build();

    given(candidateListRepository.findAllByCandidateKey(candidateKey)).willReturn(candidateListEntities);
    given(jobPostingRepository.findByJobPostingKey(jobPostingKey)).willReturn(Optional.of(jobPostingEntity));
    given(companyRepository.findByCompanyKey(companyKey)).willReturn(Optional.empty());

    // when
    Throwable throwable = catchThrowable(() -> candidateService.getApplyJobPostings(candidateKey));

    // then
    assertThat(throwable).isInstanceOf(CustomException.class);
    assertThat(((CustomException) throwable).getErrorCode()).isEqualTo(ErrorCode.COMPANY_NOT_FOUND);
  }
}