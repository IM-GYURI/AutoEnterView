package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.candidate.ChangePasswordDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto;
import com.ctrls.auto_enter_view.dto.candidate.FindEmailDto.Response;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.dto.candidate.WithdrawDto;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CandidateController {

  private final CandidateService candidateService;

  // 회원 가입
  @PostMapping("/candidates/signup")
  public ResponseEntity<?> signUp(@RequestBody @Validated SignUpDto.Request signUpDto) {

    SignUpDto.Response response = candidateService.signUp(signUpDto);

    return ResponseEntity.ok().body(response);
  }

  // 비밀번호 변경
  @PreAuthorize("hasRole('CANDIDATE')")
  @PutMapping("/candidates/{candidateKey}/password")
  public ResponseEntity<?> changePassword(
      @PathVariable String candidateKey,
      @RequestHeader("Authorization") String header,
      @RequestBody @Validated ChangePasswordDto.Request request) {

    candidateService.changePassword(candidateKey, request);

    return ResponseEntity.ok(ResponseMessage.CHANGE_PASSWORD.getMessage());
  }

  // 회원 탈퇴
  @PreAuthorize("hasRole('CANDIDATE')")
  @DeleteMapping("/candidates/withdraw/{candidateKey}")
  public ResponseEntity<?> withdraw(
      @PathVariable String candidateKey,
      @RequestHeader("Authorization") String header,
      @RequestBody @Validated WithdrawDto.Request request) {

    candidateService.withdraw(request, candidateKey);

    return ResponseEntity.ok(ResponseMessage.WITHDRAW.getMessage());
  }

  // 이메일 찾기
  @GetMapping("/candidates/find-email")
  public ResponseEntity<?> findEmail(
      @RequestBody @Validated FindEmailDto.Request request) {

    Response response = candidateService.findEmail(request);

    return ResponseEntity.ok(response);
  }
}