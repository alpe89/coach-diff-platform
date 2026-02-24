package com.coachdiff.infrastructure.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Role;
import com.coachdiff.infrastructure.adapter.out.persistence.AccountEntity;
import com.coachdiff.infrastructure.adapter.out.persistence.AccountPersistenceAdapter;
import com.coachdiff.infrastructure.adapter.out.persistence.AccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountPersistenceAdapterTest {
  @Mock private AccountRepository repository;

  private AccountPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new AccountPersistenceAdapter(repository);
  }

  @Test
  void shouldLoadAccountByEmail() {
    when(repository.findByEmail("test@email.com"))
        .thenReturn(
            Optional.of(new AccountEntity(1L, "test@email.com", "Player", "1234", "ADC", "EUW1")));

    var result = adapter.loadAccount("test@email.com");

    assertThat(result).isPresent();
    assertThat(result.get().email()).isEqualTo("test@email.com");
    assertThat(result.get().name()).isEqualTo("Player");
    assertThat(result.get().tag()).isEqualTo("1234");
    assertThat(result.get().role()).isEqualTo(Role.ADC);
    assertThat(result.get().region()).isEqualTo(Region.EUW1);
  }

  @Test
  void shouldReturnEmptyWhenAccountNotFound() {
    when(repository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

    var result = adapter.loadAccount("unknown@email.com");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldSaveAccount() {
    var account = new Account(null, "new@email.com", "NewPlayer", "5678", Role.JUNGLE, Region.EUW1);
    when(repository.save(any(AccountEntity.class)))
        .thenReturn(new AccountEntity(1L, "new@email.com", "NewPlayer", "5678", "JUNGLE", "EUW1"));

    var result = adapter.saveAccount(account);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.email()).isEqualTo("new@email.com");
    assertThat(result.name()).isEqualTo("NewPlayer");
  }

  @Test
  void shouldUpdateAccount() {
    var account = new Account(1L, "test@email.com", "Updated", "9999", Role.SUPPORT, Region.EUW1);

    adapter.updateAccount(account);

    verify(repository).save(any(AccountEntity.class));
  }

  @Test
  void shouldDeleteAccount() {
    adapter.deleteAccount(1L);

    verify(repository).deleteById(1L);
  }
}
