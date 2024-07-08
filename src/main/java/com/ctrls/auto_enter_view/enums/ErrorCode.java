package com.ctrls.auto_enter_view.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

  AUTHENTICATION_FAILED(401, "사용자 인증에 실패했습니다."),
  COMPANY_NOT_FOUND(404, "가입된 회사를 찾을 수 없습니다."),
  EMAIL_DUPLICATION(400, "이메일이 중복됩니다."),
  EMAIL_NOT_FOUND(404, "가입된 사용자 이메일이 없습니다."),
  EMAIL_SEND_FAILURE(500, "이메일 전송에 실패했습니다."),
  INTERNAL_SERVER_ERROR(500, "내부 서버 오류."),
  INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
  INVALID_VERIFICATION_CODE(400, "유효하지 않은 인증 코드입니다."),
  JOB_POSTING_NOT_FOUND(404, "채용 공고를 찾을 수 없습니다"),
  JOB_POSTING_STEP_NOT_FOUND(404, "채용 공고의 해당 단계를 찾을 수 없습니다"),
  NOT_FOUND(404, "페이지를 찾을 수 없습니다."),
  NO_AUTHORITY(401, "권한이 없습니다."),
  PASSWORD_NOT_MATCH(400, "비밀번호가 일치하지 않습니다"),
  RESUME_NOT_FOUND(404, "지원자의 이력서를 찾을 수 없습니다"),
  TOKEN_BLACKLISTED(401, "토큰이 블랙리스트에 존재하여 사용할 수 없는 토큰입니다."),
  USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다.");

  private final int status;
  private final String message;
}