package com.coachdiff.domain.port.in;

import com.coachdiff.domain.model.Account;

public interface ManageAccountPort {
  Account loadAccount(String email);

  Account saveAccount(Account account);

  void updateAccount(Account account);

  void deleteAccount(String email);
}
