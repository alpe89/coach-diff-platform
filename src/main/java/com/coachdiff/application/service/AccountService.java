package com.coachdiff.application.service;

import com.coachdiff.domain.exception.AccountNotFoundException;
import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.port.in.ManageAccountPort;
import com.coachdiff.domain.port.out.AccountPersistencePort;
import org.springframework.stereotype.Component;

@Component
public class AccountService implements ManageAccountPort {
  private final AccountPersistencePort accountPersistencePort;

  public AccountService(AccountPersistencePort accountPersistencePort) {
    this.accountPersistencePort = accountPersistencePort;
  }

  @Override
  public Account loadAccount(String email) {
    return accountPersistencePort
        .loadAccount(email)
        .orElseThrow(
            () ->
                new AccountNotFoundException(
                    ErrorCode.ACCOUNT_DATA_NOT_FOUND,
                    "Account data for " + email + " was not found"));
  }

  @Override
  public Account saveAccount(Account account) {
    return accountPersistencePort.saveAccount(account);
  }

  @Override
  public void updateAccount(Account account) {
    var loadedAccount = loadAccount(account.email());
    var combinedAccount =
        loadedAccount.withUpdates(account.name(), account.tag(), account.role(), account.region());

    accountPersistencePort.updateAccount(combinedAccount);
  }

  @Override
  public void deleteAccount(String email) {
    var account = loadAccount(email);
    accountPersistencePort.deleteAccount(account.id());
  }
}
