package com.ctrls.auto_enter_view.enums;

import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;

public enum TechStack {
  JAVA,
  SPRING_BOOT,
  NODE_JS,
  PYTHON,
  DJANGO,
  PHP,
  CPP,
  CSHARP,
  AWS,
  MYSQL,
  ORACLE,
  REACT,
  VUE_JS,
  JAVASCRIPT,
  TYPESCRIPT,
  SVELTE,
  HTML5,
  CSS3,
  ANGULAR_JS,
  JQUERY,
  KOTLIN,
  RXJAVA,
  SWIFT,
  OBJECTIVE_C,
  RXSWIFT,
  SWIFTUI,
  XCODE;

  public static TechStack fromString(String value) {

    return TechStack.valueOf(value.toUpperCase());
  }

  public ResumeTechStackEntity toEntity(String resumeKey) {

    return ResumeTechStackEntity.builder()
        .resumeKey(resumeKey)
        .techStackName(this)
        .build();
  }
}