package com.coachdiff.domain.exception;

public class MatchDataNotFoundException extends RuntimeException {
  private final ErrorCode code;

  public MatchDataNotFoundException(ErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code.name();
  }
}
