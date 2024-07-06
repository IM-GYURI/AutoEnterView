package com.ctrls.auto_enter_view.controller;

import static com.ctrls.auto_enter_view.enums.ResponseMessage.SUCCESS_EMAIL_VERIFY;
import static com.ctrls.auto_enter_view.enums.ResponseMessage.SUCCESS_SEND_CODE;
import static com.ctrls.auto_enter_view.enums.ResponseMessage.SUCCESS_TEMPORARY_PASSWORD_SEND;

import com.ctrls.auto_enter_view.dto.common.EmailDto;
import com.ctrls.auto_enter_view.dto.common.EmailVerificationDto;
import com.ctrls.auto_enter_view.dto.common.SignInDto;
import com.ctrls.auto_enter_view.dto.common.SignInDto.Request;
import com.ctrls.auto_enter_view.dto.common.SignInDto.Response;
import com.ctrls.auto_enter_view.dto.common.TemporaryPasswordDto;
import com.ctrls.auto_enter_view.service.CommonUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 없이 접근 가능 - 공통 회원 기능 : 회사, 지원자
 */

@RequestMapping("/common")
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
  public ResponseEntity<String> checkDuplicateEmail(@RequestBody @Validated EmailDto emailDto) {

    return ResponseEntity.ok(commonUserService.checkDuplicateEmail(emailDto.getEmail()));
  }

  /**
   * 이메일 인증 코드 전송
   *
   * @param emailDto
   * @return
   */
  @PostMapping("/send-verification-code")
  public ResponseEntity<String> sendVerificationCode(@RequestBody @Validated EmailDto emailDto) {

    commonUserService.sendVerificationCode(emailDto.getEmail());

    return ResponseEntity.ok(SUCCESS_SEND_CODE.getMessage());
  }

  /**
   * 이메일 인증 코드 확인
   *
   * @param emailVerificationDto
   * @return
   */
  @GetMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(
      @RequestBody @Validated EmailVerificationDto emailVerificationDto) {

    commonUserService.verifyEmailVerificationCode(emailVerificationDto.getEmail(),
        emailVerificationDto.getVerificationCode());

    return ResponseEntity.ok(SUCCESS_EMAIL_VERIFY.getMessage());
  }

  /**
   * 임시 비밀번호 전송
   *
   * @param temporaryPasswordDto
   * @return
   */
  @PostMapping("/email/password")
  public ResponseEntity<String> sendTemporaryPassword(
      @RequestBody @Validated TemporaryPasswordDto temporaryPasswordDto) {

    commonUserService.sendTemporaryPassword(temporaryPasswordDto.getEmail(),
        temporaryPasswordDto.getName());

    return ResponseEntity.ok(SUCCESS_TEMPORARY_PASSWORD_SEND.getMessage());
  }

  // 로그인
  @PostMapping("/signin")
  public ResponseEntity<SignInDto.Response> login(
      @Validated @RequestBody Request request) {

    Response response = commonUserService.loginUser(request.getEmail(), request.getPassword());
    return ResponseEntity.ok(response);
  }
}