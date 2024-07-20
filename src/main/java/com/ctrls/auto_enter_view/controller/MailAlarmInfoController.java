package com.ctrls.auto_enter_view.controller;

import static com.ctrls.auto_enter_view.enums.ResponseMessage.SUCCESS_CREATE_MAIL_ALARM;
import static com.ctrls.auto_enter_view.enums.ResponseMessage.SUCCESS_EDIT_MAIL_ALARM;

import com.ctrls.auto_enter_view.dto.mailAlarmInfo.MailAlarmInfoDto;
import com.ctrls.auto_enter_view.service.MailAlarmInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/companies/{companyKey}/job-postings/{jobPostingKey}/steps/{stepId}/mail")
public class MailAlarmInfoController {

  private final MailAlarmInfoService mailAlarmInfoService;

  /**
   * 메일 예약 생성
   *
   * @param companyKey
   * @param jobPostingKey
   * @param stepId
   * @param mailAlarmInfoDto
   * @return
   */
  @PostMapping
  public ResponseEntity<String> createMailAlarmInfo(@PathVariable String companyKey,
      @PathVariable String jobPostingKey, @PathVariable Long stepId,
      @Validated @RequestBody MailAlarmInfoDto mailAlarmInfoDto) {
    mailAlarmInfoService.createMailAlarmInfo(companyKey, jobPostingKey, stepId,
        mailAlarmInfoDto);

    return ResponseEntity.ok(SUCCESS_CREATE_MAIL_ALARM.getMessage());
  }

  /**
   * 예약된 메일 수정
   *
   * @param companyKey
   * @param jobPostingKey
   * @param stepId
   * @param mailAlarmInfoDto
   * @return
   */
  @PutMapping
  public ResponseEntity<String> editMailAlarmInfo(@PathVariable String companyKey,
      @PathVariable String jobPostingKey, @PathVariable Long stepId,
      @Validated @RequestBody MailAlarmInfoDto mailAlarmInfoDto) {
    // 여기도 수정해야 함
    mailAlarmInfoService.editMailAlarmInfo(companyKey, jobPostingKey, stepId,
        mailAlarmInfoDto);

    return ResponseEntity.ok(SUCCESS_EDIT_MAIL_ALARM.getMessage());
  }
}