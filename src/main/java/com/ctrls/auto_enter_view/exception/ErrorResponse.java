package com.ctrls.auto_enter_view.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

  private int code;
  private String message;
}