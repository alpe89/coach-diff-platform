package com.coachdiff.domain.exception;

public class LeagueDataNotFoundException extends RuntimeException {
  private final ErrorCode code;

  public LeagueDataNotFoundException(ErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code.name();
  }
}
