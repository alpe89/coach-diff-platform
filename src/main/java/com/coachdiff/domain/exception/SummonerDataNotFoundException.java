package com.coachdiff.domain.exception;

public class SummonerDataNotFoundException extends DomainNotFoundException {
  public SummonerDataNotFoundException(ErrorCode code, String message) {
    super(code, message);
  }
}
