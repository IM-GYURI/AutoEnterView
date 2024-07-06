package com.ctrls.auto_enter_view.util;

import java.util.Random;

public class RandomGenerator {

  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL = "!@#$%^&*";
  private static final String ALLCHARS =
      UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
  private static final int CODE_LENGTH = 6; // 인증 코드 길이
  private static final int PASSWORD_LENGTH = 12; // 임시 비밀번호 길이

  // 이메일 인증번호 생성
  public static String generateRandomCode() {

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < CODE_LENGTH; i++) {
      sb.append(getRandomChar(ALLCHARS));
    }

    return sb.toString();
  }

  // 임시 비밀번호 생성
  public static String generateTemporaryPassword() {

    StringBuilder sb = new StringBuilder();

    // 비밀번호에 반드시 포함되어야 할 문자 유형
    sb.append(getRandomChar(UPPERCASE));
    sb.append(getRandomChar(LOWERCASE));
    sb.append(getRandomChar(DIGITS));
    sb.append(getRandomChar(SPECIAL));

    // 나머지 문자 랜덤 선택
    for (int i = 4; i < PASSWORD_LENGTH; i++) {
      sb.append(getRandomChar(ALLCHARS));
    }

    return sb.toString();
  }

  private static char getRandomChar(String chars) {

    return chars.charAt(new Random().nextInt(chars.length()));
  }
}