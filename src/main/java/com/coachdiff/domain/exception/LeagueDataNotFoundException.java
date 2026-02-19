package com.coachdiff.domain.exception;

public class LeagueDataNotFoundException extends DomainNotFoundException {
  public LeagueDataNotFoundException(ErrorCode code, String message) {
    super(code, message);
  }
}
