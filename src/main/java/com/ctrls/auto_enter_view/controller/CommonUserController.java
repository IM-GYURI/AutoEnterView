package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.common.SignInDto.Request;
import com.ctrls.auto_enter_view.dto.common.SignInDto.Response;
import com.ctrls.auto_enter_view.service.CommonUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommonUserController {

  private final CommonUserService commonUserService;

  // 로그인
  @PostMapping("/signin")
  public ResponseEntity<?> login(
      @Validated @RequestBody Request request) {

    Response response = commonUserService.loginUser(request.getEmail(), request.getPassword());
    return ResponseEntity.ok(response);
  }
}
