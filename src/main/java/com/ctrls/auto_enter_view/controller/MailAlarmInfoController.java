package com.ctrls.auto_enter_view.controller;

import com.ctrls.auto_enter_view.service.MailAlarmInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MailAlarmInfoController {

  private final MailAlarmInfoService mailAlarmInfoService;
}