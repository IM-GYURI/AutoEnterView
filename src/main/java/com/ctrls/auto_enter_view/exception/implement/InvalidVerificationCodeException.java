package com.ctrls.auto_enter_view.exception.implement;

import com.ctrls.auto_enter_view.exception.AbstractException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidVerificationCodeException extends AbstractException {

  @Override
  public int getStatusCode() {
    return HttpStatus.BAD_REQUEST.value();
  }

  @Override
  public String getMessage() {
    return "유효하지 않은 인증 코드입니다.";
  }
}
