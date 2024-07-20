package com.ctrls.auto_enter_view.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JobCategory {
  JOB_CATEGORY_1("서버/백엔드 개발"),
  JOB_CATEGORY_2("프론트엔드 개발"),
  JOB_CATEGORY_3("웹 풀스택 개발"),
  JOB_CATEGORY_4("안드로이드 개발"),
  JOB_CATEGORY_5("iOS 개발");

  private final String value;
}