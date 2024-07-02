package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.common.EmailDto;
import com.ctrls.auto_enter_view.dto.common.EmailVerificationDto;
import com.ctrls.auto_enter_view.dto.common.TemporaryPasswordDto;
import com.ctrls.auto_enter_view.service.CommonUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 없이 접근 가능 - 공통 회원 기능 : 회사, 지원자
 */
@RequiredArgsConstructor
@RestController
public class CommonUserController {

  private final CommonUserService commonUserService;

  /**
   * 이메일 중복 확인
   *
   * @param emailDto
   * @return
   */
  @GetMapping("/duplicate-email")
  public ResponseEntity<?> checkDuplicateEmail(@RequestBody @Validated EmailDto emailDto) {
    return ResponseEntity.ok(commonUserService.checkDuplicateEmail(emailDto.getEmail()));
  }

  /**
   * 이메일 인증 코드 전송
   *
   * @param emailDto
   * @return
   */
  @PostMapping("/send-verification-code")
  public ResponseEntity<?> sendVerificationCode(@RequestBody @Validated EmailDto emailDto) {
    boolean isSent = commonUserService.sendVerificationCode(emailDto.getEmail());

    if (isSent) {
      return ResponseEntity.ok("인증 코드 전송 성공");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("인증 코드 전송 실패");
    }
  }

  /**
   * 이메일 인증 코드 확인
   *
   * @param emailVerificationDto
   * @return
   */
  @GetMapping("/verify-email")
  public ResponseEntity<?> verifyEmail(
      @RequestBody @Validated EmailVerificationDto emailVerificationDto) {
    boolean isVerified = commonUserService.verifyEmailVerificationCode(
        emailVerificationDto.getEmail(),
        emailVerificationDto.getVerificationCode());

    if (isVerified) {
      return ResponseEntity.ok("이메일 인증 성공");
    } else {
      return ResponseEntity.badRequest().body("이메일 인증 실패");
    }
  }

  /**
   * 임시 비밀번호 전송
   *
   * @param temporaryPasswordDto
   * @return
   */
  @PostMapping("/email/password")
  public ResponseEntity<?> sendTemporaryPassword(
      @RequestBody @Validated TemporaryPasswordDto temporaryPasswordDto) {
    boolean isSent = commonUserService.sendTemporaryPassword(temporaryPasswordDto.getEmail(),
        temporaryPasswordDto.getName());

    if (isSent) {
      return ResponseEntity.ok("임시 비밀번호 전송 성공");
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("임시 비밀번호 전송 실패");
    }
  }
}
