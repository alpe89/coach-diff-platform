package com.coachdiff.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.exception.AccountNotFoundException;
import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.port.out.AccountPersistencePort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
  @Mock private AccountPersistencePort accountPersistencePort;

  private AccountService accountService;

  @BeforeEach
  void setUp() {
    accountService = new AccountService(accountPersistencePort);
  }

  @Test
  void shouldLoadAccount() {
    when(accountPersistencePort.loadAccount(anyString())).thenReturn(Optional.of(createAccount()));

    var account = accountService.loadAccount("email@user.com");
    assertThat(account)
        .extracting(Account::id, Account::email)
        .containsExactly(12345L, "email@user.com");
  }

  @Test
  void shouldThrowAccountNotFoundExceptionWhenAccountNotFound() {
    when(accountPersistencePort.loadAccount(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountService.loadAccount("email@user.com"))
        .isInstanceOf(AccountNotFoundException.class);
  }

  @Test
  void shouldCreateAccount() {
    var account = createAccount();
    when(accountPersistencePort.saveAccount(account)).thenReturn(account);

    var createdAccount = accountService.saveAccount(account);

    assertThat(createdAccount).isEqualTo(account);
  }

  @Test
  void shouldUpdateAccount() {
    when(accountPersistencePort.loadAccount("email@user.com"))
        .thenReturn(Optional.of(createAccount()));

    var account = new Account(12345L, "email@user.com", "Jhonny", "1234", Role.MID, Region.KR);

    accountService.updateAccount(account);

    ArgumentCaptor<Account> captor = ArgumentCaptor.captor();
    verify(accountPersistencePort).updateAccount(captor.capture());

    var savedAccount = captor.getValue();

    assertThat(savedAccount.id()).isEqualTo(12345L);
    assertThat(savedAccount.email()).isEqualTo("email@user.com");
    assertThat(savedAccount.name()).isEqualTo("Jhonny");
    assertThat(savedAccount.tag()).isEqualTo("1234");
    assertThat(savedAccount.role()).isEqualTo(Role.MID);
    assertThat(savedAccount.region()).isEqualTo(Region.KR);
  }

  @Test
  void shouldThrowWhenUpdatingNonExistentAccount() {
    when(accountPersistencePort.loadAccount("unknown@email.com")).thenReturn(Optional.empty());

    var account = new Account(12345L, "unknown@email.com", "Jhonny", "1234", Role.MID, Region.KR);

    assertThatThrownBy(() -> accountService.updateAccount(account))
        .isInstanceOf(AccountNotFoundException.class);
  }

  @Test
  void shouldDeleteAccount() {
    when(accountPersistencePort.loadAccount("email@user.com"))
        .thenReturn(Optional.of(createAccount()));

    accountService.deleteAccount("email@user.com");

    verify(accountPersistencePort).deleteAccount(12345L);
  }

  @Test
  void shouldThrowWhenDeletingNonExistentAccount() {
    when(accountPersistencePort.loadAccount("unknown@email.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> accountService.deleteAccount("unknown@email.com"))
        .isInstanceOf(AccountNotFoundException.class);
  }

  private Account createAccount() {
    return new Account(12345L, "email@user.com", "summoner-name", "1234", Role.JUNGLE, Region.EUW1);
  }
}
