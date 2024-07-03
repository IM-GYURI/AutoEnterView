package com.ctrls.auto_enter_view.exception.implement;

import com.ctrls.auto_enter_view.enums.ErrorCode;
import com.ctrls.auto_enter_view.exception.AbstractException;
import lombok.Getter;

@Getter
public class VerificationCodeSendFailedException extends AbstractException {

  private final ErrorCode errorCode;

  public VerificationCodeSendFailedException (ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
