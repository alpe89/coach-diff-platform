package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.Account;
import java.util.Optional;

public interface LoadAccountPort {
  Optional<Account> loadAccount(String email);
}
