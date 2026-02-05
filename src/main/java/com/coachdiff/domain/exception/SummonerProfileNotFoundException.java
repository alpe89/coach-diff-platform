package com.coachdiff.domain.exception;

public class SummonerProfileNotFoundException extends RuntimeException {
  private final ErrorCode code;

  public SummonerProfileNotFoundException(ErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code.name();
  }
}
