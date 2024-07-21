package com.ctrls.auto_enter_view.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JobCategory {

  BACKEND("서버/백엔드 개발"),
  FRONTEND("프론트엔드 개발"),
  FULL("웹 풀스택 개발"),
  ANDROID("안드로이드 개발"),
  IOS("iOS 개발");

  private final String value;
}