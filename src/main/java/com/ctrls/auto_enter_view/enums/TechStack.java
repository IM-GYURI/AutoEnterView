package com.ctrls.auto_enter_view.enums;

import com.ctrls.auto_enter_view.entity.ResumeTechStackEntity;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TechStack {
  JAVA("Java"),
  SPRING_BOOT("Spring Boot"),
  NODE_JS("Node.js"),
  PYTHON("Python"),
  DJANGO("Django"),
  PHP("PHP"),
  CPP("C++"),
  CSHARP("C#"),
  AWS("AWS"),
  MYSQL("MySQL"),
  ORACLE("Oracle"),
  REACT("React"),
  VUE_JS("Vue.js"),
  JAVASCRIPT("JavaScript"),
  TYPESCRIPT("TypeScript"),
  SVELTE("Svelte"),
  HTML5("HTML5"),
  CSS3("CSS3"),
  ANGULARJS("AngularJS"),
  JQUERY("jQuery"),
  KOTLIN("Kotlin"),
  RXJAVA("RxJava"),
  SWIFT("Swift"),
  OBJECTIVE_C("Objective-C"),
  RXSWIFT("Rxswift"),
  SWIFTUI("SwiftUI"),
  XCODE("XCODE");

  private final String value;

  private static final Map<String, TechStack> TECH_STACK_MAP = new HashMap<>();

  static {
    for (TechStack tech : TechStack.values()) {
      TECH_STACK_MAP.put(tech.getValue().toUpperCase(), tech);
    }
  }

  public static TechStack parse(String value) {

    return TECH_STACK_MAP.get(value);
  }

  public ResumeTechStackEntity toEntity(String resumeKey) {

    return ResumeTechStackEntity.builder()
        .resumeKey(resumeKey)
        .techStackName(this)
        .build();
  }
}