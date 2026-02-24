package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.Account;
import java.util.Optional;

public interface AccountPersistencePort {
  Optional<Account> loadAccount(String email);

  Account saveAccount(Account account);

  void updateAccount(Account account);

  void deleteAccount(Long id);
}
