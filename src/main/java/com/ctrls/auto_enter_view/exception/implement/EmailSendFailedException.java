package com.ctrls.auto_enter_view.exception.implement;

import com.ctrls.auto_enter_view.exception.AbstractException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmailSendFailedException extends AbstractException {

  @Override
  public int getStatusCode() {
    return HttpStatus.INTERNAL_SERVER_ERROR.value();
  }

  @Override
  public String getMessage() {
    return "이메일 전송에 실패했습니다.";
  }
}
