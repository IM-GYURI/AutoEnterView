package com.ctrls.auto_enter_view.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AbstractException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(AbstractException ex) {

    log.error("handleCustomException", ex);
    ErrorResponse response = new ErrorResponse(ex.getErrorCode());
    return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getErrorCode().getStatus()));
  }
}