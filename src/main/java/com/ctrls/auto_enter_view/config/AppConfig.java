package com.ctrls.auto_enter_view.config;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AppConfig {

  /**
   * 기본 시간 설정 값 Asia/Seoul로 변경
   */
  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    log.info("기본 시간대를 Asia/Seoul로 설정 완료");
  }
}