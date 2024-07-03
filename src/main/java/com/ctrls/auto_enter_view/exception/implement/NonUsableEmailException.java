package com.ctrls.auto_enter_view.exception.implement;

import com.ctrls.auto_enter_view.exception.AbstractException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NonUsableEmailException extends AbstractException {

  @Override
  public int getStatusCode() {
    return HttpStatus.BAD_REQUEST.value();
  }

  @Override
  public String getMessage() {
    return "사용할 수 없는 이메일입니다.";
  }
}
