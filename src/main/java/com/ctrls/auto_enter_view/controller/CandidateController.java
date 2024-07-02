package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.candidate.SignUpDto;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto.Request;
import com.ctrls.auto_enter_view.dto.candidate.SignUpDto.Response;
import com.ctrls.auto_enter_view.dto.candidate.WithdrawDto;
import com.ctrls.auto_enter_view.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CandidateController {

  private final CandidateService candidateService;

  @PostMapping("/candidates/signup")
  public ResponseEntity<?> signUp(@RequestBody @Validated Request signUpDto) {
    candidateService.signUp(signUpDto);
    SignUpDto.Response response = candidateService.getSignUpDto(signUpDto);
    return ResponseEntity.ok().body(response);
  }

  @DeleteMapping("/candidates/withdraw/{candidateKey}")
  public ResponseEntity<?> withdraw(@PathVariable String candidateKey,
      @RequestBody @Validated WithdrawDto.Request request) {
    candidateService.withdraw(request, candidateKey);
    return ResponseEntity.ok("회원 탈퇴 완료");
  }

}
