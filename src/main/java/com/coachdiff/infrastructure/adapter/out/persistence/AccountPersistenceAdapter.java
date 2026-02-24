package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.port.out.AccountPersistencePort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AccountPersistenceAdapter implements AccountPersistencePort {
  private final AccountRepository repository;

  public AccountPersistenceAdapter(AccountRepository accountRepository) {
    this.repository = accountRepository;
  }

  @Override
  public Optional<Account> loadAccount(String email) {
    return repository.findByEmail(email).map(AccountEntity::toDomain);
  }

  @Override
  public Account saveAccount(Account account) {
    return AccountEntity.toDomain(repository.save(AccountEntity.fromDomain(account)));
  }

  @Override
  public void updateAccount(Account account) {
    repository.save(AccountEntity.fromDomain(account));
  }

  @Override
  public void deleteAccount(Long id) {
    repository.deleteById(id);
  }
}
