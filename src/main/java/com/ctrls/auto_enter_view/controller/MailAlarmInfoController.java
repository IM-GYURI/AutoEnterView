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
@RequestMapping("/companies")
public class MailAlarmInfoController {

  private final MailAlarmInfoService mailAlarmInfoService;

  /**
   * 메일 예약 생성
   *
   * @param companyKey
   * @param interviewScheduleKey
   * @param stepId
   * @param mailAlarmInfoDto
   * @return
   */
  @PostMapping("/{companyKey}/interview-schedules/{interviewScheduleKey}/steps/{stepId}/mail")
  public ResponseEntity<String> createMailAlarmInfo(@PathVariable String companyKey,
      @PathVariable String interviewScheduleKey, @PathVariable Long stepId,
      @Validated @RequestBody MailAlarmInfoDto mailAlarmInfoDto) {
    mailAlarmInfoService.createMailAlarmInfo(companyKey, interviewScheduleKey, stepId,
        mailAlarmInfoDto);

    return ResponseEntity.ok(SUCCESS_CREATE_MAIL_ALARM.getMessage());
  }

  /**
   * 예약된 메일 수정
   *
   * @param companyKey
   * @param interviewScheduleKey
   * @param stepId
   * @param mailAlarmInfoDto
   * @return
   */
  @PutMapping("/{companyKey}/interview-schedules/{interviewScheduleKey}/steps/{stepId}/mail")
  public ResponseEntity<String> editMailAlarmInfo(@PathVariable String companyKey,
      @PathVariable String interviewScheduleKey, @PathVariable Long stepId,
      @Validated @RequestBody MailAlarmInfoDto mailAlarmInfoDto) {
    mailAlarmInfoService.editMailAlarmInfo(companyKey, interviewScheduleKey, stepId,
        mailAlarmInfoDto);

    return ResponseEntity.ok(SUCCESS_EDIT_MAIL_ALARM.getMessage());
  }
}