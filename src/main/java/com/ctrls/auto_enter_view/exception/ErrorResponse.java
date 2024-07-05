package com.ctrls.auto_enter_view.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

  private int code;
  private String message;
}