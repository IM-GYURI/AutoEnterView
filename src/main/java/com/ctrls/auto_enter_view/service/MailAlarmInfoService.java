package com.ctrls.auto_enter_view.service;

import com.ctrls.auto_enter_view.repository.MailAlarmInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailAlarmInfoService {

  private final MailAlarmInfoRepository mailAlarmInfoRepository;
}
