package com.coachdiff.domain.exception;

public class BenchmarkNotFoundException extends DomainNotFoundException {
  public BenchmarkNotFoundException(ErrorCode code, String message) {
    super(code, message);
  }
}
