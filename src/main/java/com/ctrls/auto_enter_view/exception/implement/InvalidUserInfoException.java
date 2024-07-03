package com.ctrls.auto_enter_view.exception.implement;

import com.ctrls.auto_enter_view.exception.AbstractException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidUserInfoException extends AbstractException {

  @Override
  public int getStatusCode() {
    return HttpStatus.NOT_FOUND.value();
  }

  @Override
  public String getMessage() {
    return "작성해주신 계정 정보가 올바르지 않습니다.";
  }
}
