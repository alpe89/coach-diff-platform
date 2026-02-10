package com.coachdiff.infrastructure.adapter.out.exception;

public class RiotUnknownException extends RiotException {
  public RiotUnknownException(int status, String message) {
    super(status, message);
  }
}
