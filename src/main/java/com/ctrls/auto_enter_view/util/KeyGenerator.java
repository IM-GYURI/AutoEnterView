package com.ctrls.auto_enter_view.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class KeyGenerator {

  public static String generateKey() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
