package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.candidate.WithdrawDto;
import com.ctrls.auto_enter_view.dto.company.ChangePasswordDto;
import com.ctrls.auto_enter_view.dto.company.SignUpDto;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CompanyController {

  private final CompanyService companyService;

  @PostMapping("/companies/signup")
  public ResponseEntity<?> signUp(
      @Validated @RequestBody SignUpDto.Request form) {

    SignUpDto.Response response = companyService.signUp(form);

    return ResponseEntity.ok(response);
  }

  @PutMapping("/companies/{companyKey}/password")
  public ResponseEntity<?> changePassword(
      @PathVariable String companyKey,
      @Validated @RequestBody ChangePasswordDto.Request form) {

    companyService.changePassword(companyKey, form);

    return ResponseEntity.ok(ResponseMessage.CHANGE_PASSWORD.getMessage());
  }

  @DeleteMapping("/candidates/withdraw/{companyKey}")
  public ResponseEntity<?> withdraw(@PathVariable String companyKey,
      @RequestBody @Validated WithdrawDto.Request request) {
    companyService.withdraw(request, companyKey);
    return ResponseEntity.ok("회원 탈퇴 완료");
  }


}