package com.ctrls.auto_enter_view.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .code(e.getErrorCode().getStatus())
        .message(e.getMessage())
        .build();

    log.error("handleCustomException", e);
    return new ResponseEntity<>(errorResponse, new HttpHeaders(),
        HttpStatus.valueOf(e.getErrorCode().getStatus()));
  }
}