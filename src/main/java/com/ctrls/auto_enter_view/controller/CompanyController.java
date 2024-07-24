package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.company.SignUpDto;
import com.ctrls.auto_enter_view.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CompanyController {

  private final CompanyService companyService;

  /**
   * 회사 회원가입
   *
   * @param request SignUp.Request
   * @return SignUp.Response
   */
  @PostMapping("/companies/signup")
  public ResponseEntity<SignUpDto.Response> signUp(
      @Validated @RequestBody SignUpDto.Request request) {

    SignUpDto.Response response = companyService.signUp(request);

    return ResponseEntity.ok(response);
  }
}