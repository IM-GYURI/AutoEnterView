package com.ctrls.auto_enter_view.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseMessage {
  SIGNUP("회원가입을 축하드립니다."),
  CHANGE_PASSWORD("비밀번호 변경 완료."),
  SUCCESS_SEND_CODE("인증 코드 전송 성공."),
  SUCCESS_EMAIL_VERIFY("이메일 인증 성공."),
  WITHDRAW("회원탈퇴 완료");

  private final String message;
}