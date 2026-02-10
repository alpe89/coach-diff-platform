package com.coachdiff.infrastructure.adapter.out.exception;

public class RiotAuthenticationException extends RiotException {
  public RiotAuthenticationException(int status, String message) {
    super(status, message);
  }
}
