package com.ctrls.auto_enter_view.exception;

import com.ctrls.auto_enter_view.enums.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {

    super(String.valueOf(errorCode.getStatus()));
    this.errorCode = errorCode;
  }
}