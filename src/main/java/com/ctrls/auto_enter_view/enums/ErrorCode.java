package com.ctrls.auto_enter_view.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

  NOT_FOUND(404, "COMMON-ERR-404", "페이지를 찾을 수 없습니다."),
  INTER_SERVER_ERROR(500, "COMMON-ERR-500", "내부 서버 오류"),
  EMAIL_DUPLICATION(400, "USER-ERR-400", "이메일이 중복됩니다."),
  USER_NOT_FOUND(404, "USER-ERR-404", "사용자를 찾을 수 없습니다."),
  EMAIL_NOT_FOUND(404, "USER-ERR-404", "가입된 사용자 이메일이 없습니다."),
  EMAIL_SEND_FAILURE(500, "USER-ERR-500", "이메일 전송에 실패했습니다."),
  INVALID_VERIFICATION_CODE(400, "USER-ERR-400", "유효하지 않은 인증 코드입니다.");

  private final int status;
  private final String errorCode;
  private final String message;
}
