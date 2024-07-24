package com.ctrls.auto_enter_view.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

  AUTHENTICATION_FAILED(401, "사용자 인증에 실패했습니다."),
  COMPANY_NOT_FOUND(404, "가입된 회사를 찾을 수 없습니다."),
  CANDIDATE_NOT_FOUND(404, "가입된 지원자를 찾을 수 없습니다."),
  EMAIL_DUPLICATION(409, "이메일이 중복됩니다."),
  COMPANY_NUMBER_DUPLICATION(409, "회사 전화번호가 중복됩니다."),
  EMAIL_NOT_FOUND(404, "가입된 사용자 이메일이 없습니다."),
  EMAIL_SEND_FAILURE(500, "이메일 전송에 실패했습니다."),
  INTERNAL_SERVER_ERROR(500, "내부 서버 오류입니다."),
  INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
  INVALID_VERIFICATION_CODE(400, "유효하지 않은 인증 코드입니다."),
  JOB_POSTING_NOT_FOUND(404, "채용 공고를 찾을 수 없습니다."),
  JOB_POSTING_STEP_NOT_FOUND(404, "채용 공고의 해당 단계를 찾을 수 없습니다."),
  JOB_POSTING_HAS_CANDIDATES(409, "채용 공고에 이미 지원한 지원자가 존재합니다."),
  NOT_FOUND(404, "페이지를 찾을 수 없습니다."),
  NO_AUTHORITY(401, "권한이 없습니다."),
  INVALID_EMAIL_OR_PASSWORD(401, "이메일 또는 비밀번호가 일치하지 않습니다."),
  PASSWORD_NOT_MATCH(400, "비밀번호가 일치하지 않습니다."),
  APPLICANT_NOT_FOUND(404, "지원자를 찾을 수 없습니다."),
  RESUME_NOT_FOUND(404, "지원자의 이력서를 찾을 수 없습니다"),
  TOKEN_BLACKLISTED(401, "토큰이 블랙리스트에 존재하여 사용할 수 없는 토큰입니다."),
  USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
  KEY_NOT_MATCH(400, "키가 일치하지 않습니다."),
  USER_NOT_FOUND_BY_NAME_AND_PHONE(404, "입력한 이름과 전화번호로 등록된 사용자를 찾을 수 없습니다."),
  ALREADY_APPLIED(409, "이미 지원한 채용 공고입니다."),
  APPLY_NOT_FOUND(404, "채용 공고에 지원한 정보를 찾을 수 없습니다."),
  INTERVIEW_SCHEDULE_NOT_FOUND(404, "해당 면접 일정을 찾을 수 없습니다."),
  MAIL_ALARM_TIME_BEFORE_NOW(400, "메일 예약 발송 시간은 현재 이후여야 합니다."),
  MAIL_ALARM_INFO_NOT_FOUND(404, "예약된 메일 내역을 찾을 수 없습니다."),
  S3_UPLOAD_ERROR(500, "S3에 이미지를 업로드하는 중 오류가 발생했습니다."),
  IMAGE_NOT_FOUND(404, "이미지를 찾을 수 없습니다."),
  FILE_SIZE_EXCEEDED(400, "파일 크기가 너무 큽니다."),
  INVALID_FILE_FORMAT(400, "지원하지 않는 파일 형식입니다."),
  FAILED_TO_DELETE_IMAGE(500, "이미지 삭제에 실패했습니다."),
  INVALID_IMAGE_URL(400, "잘못된 이미지 URL 입니다."),
  ALREADY_EXISTS(409, "이미 존재합니다."),
  CANDIDATE_INADEQUATE_ERROR(404, "일정 생성 수 보다 지원자가 부족합니다."),
  JOB_POSTING_KEY_NOT_FOUND(404, "채용 공고 KEY 를 찾지 못했습니다."),
  NEXT_STEP_NOT_FOUND(404, "다음 단계가 존재하지 않습니다."),
  JOB_POSTING_EXPIRED(404, "마감일이 지난 채용 공고 입니다."),
  BLACKLIST_TOKEN_ADD_FAILED(500, "블랙리스트 토큰 추가에 실패했습니다.");

  private final int status;
  private final String message;
}