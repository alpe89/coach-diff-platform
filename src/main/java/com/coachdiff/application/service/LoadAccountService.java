package com.coachdiff.application.service;

import com.coachdiff.domain.exception.AccountNotFoundException;
import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.port.in.FetchAccountPort;
import com.coachdiff.domain.port.out.LoadAccountPort;
import org.springframework.stereotype.Component;

@Component
public class LoadAccountService implements FetchAccountPort {
  private final LoadAccountPort loadAccountPort;

  public LoadAccountService(LoadAccountPort loadAccountPort) {
    this.loadAccountPort = loadAccountPort;
  }

  @Override
  public Account loadAccount(String email) {
    return loadAccountPort
        .loadAccount(email)
        .orElseThrow(
            () ->
                new AccountNotFoundException(
                    ErrorCode.ACCOUNT_DATA_NOT_FOUND,
                    "Account data for " + email + " was not found"));
  }
}
