package com.coachdiff.infrastructure.adapter.out.exception;

public class RiotRateLimitException extends RiotException {
  public RiotRateLimitException(int status, String message) {
    super(status, message);
  }
}
