package com.coachdiff.domain.exception;

public abstract class DomainNotFoundException extends RuntimeException {
  private final ErrorCode code;

  protected DomainNotFoundException(ErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code.name();
  }
}
