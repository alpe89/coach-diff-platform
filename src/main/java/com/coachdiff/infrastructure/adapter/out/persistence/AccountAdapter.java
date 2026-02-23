package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.port.out.LoadAccountPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AccountAdapter implements LoadAccountPort {
  private final AccountRepository repository;

  public AccountAdapter(AccountRepository accountRepository) {
    this.repository = accountRepository;
  }

  @Override
  public Optional<Account> loadAccount(String email) {
    return repository.findByEmail(email).map(AccountEntity::toDomain);
  }
}
