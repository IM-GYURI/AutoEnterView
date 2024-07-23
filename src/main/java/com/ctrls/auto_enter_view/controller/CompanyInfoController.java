package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.dto.company.CreateCompanyInfoDto.Request;
import com.ctrls.auto_enter_view.dto.company.ReadCompanyInfoDto.Response;
import com.ctrls.auto_enter_view.enums.ResponseMessage;
import com.ctrls.auto_enter_view.service.CompanyInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

  /**
   * 회사 정보 생성하기
   *
   * @param companyKey 회사 PK
   * @param request CreateCompanyInfoDto.Request
   * @return ResponseMessage
   */
  @PostMapping
  public ResponseEntity<String> createInfo(
      @PathVariable String companyKey,
      @RequestBody @Validated Request request
  ) {

    companyInfoService.createInfo(companyKey, request);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_CREATE_COMPANY_INFO.getMessage());
  }

  /**
   * 회사 정보 조회하기
   *
   * @param companyKey 회사 PK
   * @return ReadCompanyInfoDto.Response
   */
  @GetMapping
  public ResponseEntity<Response> readInfo(
      @PathVariable String companyKey
  ) {

    Response response = companyInfoService.readInfo(companyKey);

    return ResponseEntity.ok(response);
  }

  /**
   * 회사 정보 수정하기
   *
   * @param companyKey 회사 PK
   * @param request CreateCompanyInfoDto.Request
   * @return ResponseMessage
   */
  @PutMapping
  public ResponseEntity<String> updateInfo(
      @PathVariable String companyKey,
      @RequestBody @Validated Request request
  ) {

    companyInfoService.updateInfo(companyKey, request);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_UPDATE_COMPANY_INFO.getMessage());
  }

  /**
   * 회사 정보 삭제하기
   *
   * @param companyKey 회사 PK
   * @return ResponseMessage
   */
  @DeleteMapping
  public ResponseEntity<String> deleteInfo(
      @PathVariable String companyKey
  ) {

    companyInfoService.deleteInfo(companyKey);

    return ResponseEntity.ok(ResponseMessage.SUCCESS_DELETE_COMPANY_INFO.getMessage());
  }
}