package com.ctrls.auto_enter_view.component;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * 인조 식별자 생성을 위한 UUID 생성 객체
 */

@Component
public class KeyGenerator {

  public String generateKey() {

    return UUID.randomUUID().toString().replace("-", "");
  }
}