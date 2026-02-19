package com.coachdiff.domain.exception;

public class SummonerProfileNotFoundException extends DomainNotFoundException {
  public SummonerProfileNotFoundException(ErrorCode code, String message) {
    super(code, message);
  }
}
