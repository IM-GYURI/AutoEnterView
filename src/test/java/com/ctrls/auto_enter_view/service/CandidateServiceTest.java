package com.ctrls.auto_enter_view.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ctrls.auto_enter_view.dto.candidate.ChangePasswordDto;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.dto.candidate.WithdrawDto;
import com.ctrls.auto_enter_view.entity.CandidateEntity;
import com.ctrls.auto_enter_view.enums.UserRole;
import com.ctrls.auto_enter_view.repository.CandidateRepository;
import com.ctrls.auto_enter_view.util.KeyGenerator;
import java.util.ArrayList;
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

    CandidateEntity savedCandidate = CandidateEntity.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(encodedPassword)
        .role(UserRole.ROLE_CANDIDATE)
        .candidateKey(KeyGenerator.generateKey())
        .phoneNumber(request.getPhoneNumber())
        .build();

    given(candidateRepository.save(any(CandidateEntity.class))).willReturn(savedCandidate);

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

}
