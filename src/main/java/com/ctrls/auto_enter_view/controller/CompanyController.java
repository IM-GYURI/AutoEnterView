package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.company.SignUpDto;
import com.ctrls.auto_enter_view.dto.company.WithdrawDto;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.CompanyService;
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
public class CompanyController {

  private final CompanyService companyService;

  // 회원 가입
  @PostMapping("/companies/signup")
  public ResponseEntity<SignUpDto.Response> signUp(
      @Validated @RequestBody SignUpDto.Request form) {

    SignUpDto.Response response = companyService.signUp(form);

    return ResponseEntity.ok(response);
  }

  // 회원 탈퇴
  @DeleteMapping("/companies/withdraw/{companyKey}")
  public ResponseEntity<String> withdraw(@PathVariable String companyKey) {

    companyService.withdraw(companyKey);

    return ResponseEntity.ok(ResponseMessage.WITHDRAW.getMessage());
  }
}