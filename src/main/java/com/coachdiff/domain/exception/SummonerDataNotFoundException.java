package com.coachdiff.domain.exception;

public class SummonerDataNotFoundException extends RuntimeException {
  private final ErrorCode code;

  public SummonerDataNotFoundException(ErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code.name();
  }
}
