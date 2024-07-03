package com.ctrls.auto_enter_view.exception;

import com.ctrls.auto_enter_view.enums.ErrorCode;

public abstract class AbstractException extends RuntimeException {

  public abstract ErrorCode getErrorCode();

  public AbstractException(String message) {

    super(message);
  }

}
