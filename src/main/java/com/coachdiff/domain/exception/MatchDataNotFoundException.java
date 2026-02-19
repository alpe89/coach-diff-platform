package com.coachdiff.domain.exception;

public class MatchDataNotFoundException extends DomainNotFoundException {
  public MatchDataNotFoundException(ErrorCode code, String message) {
    super(code, message);
  }
}
