package com.ctrls.auto_enter_view.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Education {
  EDUCATION_0("학력무관", 0),
  EDUCATION_1("중졸 이하", 0),
  EDUCATION_2("고졸", 5),
  EDUCATION_3("대학 2,3년제", 10),
  EDUCATION_4("대학 4년제", 15),
  EDUCATION_5("석사", 20),
  EDUCATION_6("박사", 25);

  private final String value;
  private final int score;
}