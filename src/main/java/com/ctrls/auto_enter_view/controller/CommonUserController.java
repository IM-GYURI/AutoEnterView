package com.ctrls.auto_enter_view.controller;

import static com.ctrls.auto_enter_view.enums.ResponseMessage.SUCCESS_EMAIL_VERIFY;
import static com.ctrls.auto_enter_view.enums.ResponseMessage.SUCCESS_SEND_CODE;
import static com.ctrls.auto_enter_view.enums.ResponseMessage.SUCCESS_TEMPORARY_PASSWORD_SEND;

import com.ctrls.auto_enter_view.dto.common.ChangePasswordDto;
import com.ctrls.auto_enter_view.dto.common.EmailDto;
import com.ctrls.auto_enter_view.dto.common.EmailVerificationDto;
import com.ctrls.auto_enter_view.dto.common.SignInDto;
import com.ctrls.auto_enter_view.dto.common.SignInDto.Request;
import com.ctrls.auto_enter_view.dto.common.TemporaryPasswordDto;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.security.JwtTokenProvider;
import com.ctrls.auto_enter_view.service.CommonUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 없이 접근 가능 - 공통 회원 기능 : 회사, 지원자
 */
@RequestMapping("/common")
@RequiredArgsConstructor
@RestController
@Slf4j
public class CommonUserController {

  private final CommonUserService commonUserService;
  private final JwtTokenProvider jwtTokenProvider;


  /**
   * 이메일 중복 확인
   *
   * @param emailDto requestDto
   * @return ResponseMessage
   */
  @PostMapping("/duplicate-email")
  public ResponseEntity<String> checkDuplicateEmail(@RequestBody @Validated EmailDto emailDto) {

    return ResponseEntity.ok(commonUserService.checkDuplicateEmail(emailDto.getEmail()));
  }

  /**
   * 이메일 인증 코드 전송
   *
   * @param emailDto requestDto
   * @return ResponseMessage
   */
  @PostMapping("/send-verification-code")
  public ResponseEntity<String> sendVerificationCode(@RequestBody @Validated EmailDto emailDto) {

    commonUserService.sendVerificationCode(emailDto.getEmail());

    return ResponseEntity.ok(SUCCESS_SEND_CODE.getMessage());
  }

  /**
   * 이메일 인증 코드 확인
   *
   * @param emailVerificationDto RequestDto
   * @return ResponseMessage
   */
  @PostMapping("/verify-email")
  public ResponseEntity<String> verifyEmail(
      @RequestBody @Validated EmailVerificationDto emailVerificationDto) {

    commonUserService.verifyEmailVerificationCode(emailVerificationDto.getEmail(),
        emailVerificationDto.getVerificationCode());

    return ResponseEntity.ok(SUCCESS_EMAIL_VERIFY.getMessage());
  }

  /**
   * 임시 비밀번호 전송
   *
   * @param temporaryPasswordDto RequestDto
   * @return ResponseMessage
   */
  @PostMapping("/email/password")
  public ResponseEntity<String> sendTemporaryPassword(
      @RequestBody @Validated TemporaryPasswordDto temporaryPasswordDto) {

    commonUserService.sendTemporaryPassword(temporaryPasswordDto.getEmail(),
        temporaryPasswordDto.getName());

    return ResponseEntity.ok(SUCCESS_TEMPORARY_PASSWORD_SEND.getMessage());
  }

  /**
   * 로그인
   * @param request SignInDto.Request
   * @return SignInDto.Response + Header token
   */
  @PostMapping("/signin")
  public ResponseEntity<SignInDto.Response> login(@Validated @RequestBody Request request) {

    SignInDto.Response response = commonUserService.loginUser(request.getEmail(),
        request.getPassword());

    // JWT 토큰 생성
    String token = jwtTokenProvider.generateToken(response.getEmail(), response.getRole());

    // 응답 헤더에 JWT 토큰 추가
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

    return ResponseEntity.ok()
        .headers(httpHeaders)
        .body(response);
  }

  /**
   * 로그 아웃
   *
   * @param token 토큰 정보
   * @return ResponseMessage
   */
  @PostMapping("/signout")
  public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {

    log.info(token);
    commonUserService.logoutUser(token);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_LOGOUT.getMessage());
  }

  /**
   * 비밀번호 변경하기
   *
   * @param key CompanyKey 또는 CandidateKey
   * @param request ChangePasswordDto.Request
   * @return ResponseMessage
   */
  @PutMapping("/{key}/password")
  public ResponseEntity<String> changePassword(@PathVariable String key, @RequestBody @Validated
  ChangePasswordDto.Request request) {
    commonUserService.changePassword(key, request);
    return ResponseEntity.ok(ResponseMessage.CHANGE_PASSWORD.getMessage());
  }

  /**
   * 사용자 탈퇴하기
   *
   * @param key CompanyKey 또는 CandidateKey
   * @return ResponseMessage
   */
  @DeleteMapping("/{key}/withdraw")
  public ResponseEntity<String> withdraw(@PathVariable String key) {

    commonUserService.withdraw(key);

    return ResponseEntity.ok(ResponseMessage.WITHDRAW.getMessage());
  }
}