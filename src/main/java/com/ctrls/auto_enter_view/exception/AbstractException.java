package com.ctrls.auto_enter_view.exception;

public abstract class AbstractException extends RuntimeException {

  public abstract int getStatusCode();

  @Override
  public abstract String getMessage();
  
}
