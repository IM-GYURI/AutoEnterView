package com.ctrls.auto_enter_view.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Education {
  NONE("학력무관", 0),
  MIDDLE_SCHOOL("중졸 이하", 0),
  HIGH_SCHOOL("고졸", 5),
  ASSOCIATE("대학 2,3년제", 10),
  BACHELOR("대학 4년제", 15),
  MASTER("석사", 20),
  DOCTORATE("박사", 25);

  private final String label;
  private final int score;

}