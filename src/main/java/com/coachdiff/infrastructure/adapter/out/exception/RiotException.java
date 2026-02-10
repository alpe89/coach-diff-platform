package com.coachdiff.infrastructure.adapter.out.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RiotException extends RuntimeException {
  private static final Logger log = LoggerFactory.getLogger(RiotException.class);
  private final int status;

  public RiotException(int status, String message) {
    super(message);
    this.status = status;

    log.warn("Riot API returned {} error: {}", status, message);
  }

  public int getStatus() {
    return this.status;
  }
}
