package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.company.CreateCompanyInfoDto.Request;
import com.ctrls.auto_enter_view.dto.company.ReadCompanyInfoDto.Response;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.CompanyInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/companies/{companyKey}/information")
@RequiredArgsConstructor
@RestController
public class CompanyInfoController {

  private final CompanyInfoService companyInfoService;

  @PostMapping
  public ResponseEntity<String> createInfo(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String companyKey, @RequestBody @Validated Request request
  ) {
    companyInfoService.createInfo(userDetails, companyKey, request);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_CREATE_COMPANY_INFO.getMessage());
  }

  @GetMapping
  public ResponseEntity<Response> readInfo(
      @PathVariable String companyKey
  ) {
    Response response = companyInfoService.readInfo(companyKey);

    return ResponseEntity.ok(response);
  }

  @PutMapping
  public ResponseEntity<String> updateInfo(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String companyKey, @RequestBody @Validated Request request
  ) {
    companyInfoService.updateInfo(userDetails, companyKey, request);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_UPDATE_COMPANY_INFO.getMessage());
  }

  @DeleteMapping
  public ResponseEntity<String> deleteInfo(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String companyKey
  ) {
    companyInfoService.deleteInfo(userDetails, companyKey);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_DELETE_COMPANY_INFO.getMessage());
  }
}