package com.coachdiff.domain.exception;

public class AccountNotFoundException extends DomainNotFoundException {
  public AccountNotFoundException(ErrorCode code, String message) {
    super(code, message);
  }
}
