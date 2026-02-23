package com.coachdiff.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.exception.AccountNotFoundException;
import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.port.out.LoadAccountPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoadAccountServiceTest {
  @Mock private LoadAccountPort loadAccountPort;

  private LoadAccountService loadAccountService;

  @BeforeEach
  void setUp() {
    loadAccountService = new LoadAccountService(loadAccountPort);
  }

  @Test
  void shouldLoadAccount() {
    when(loadAccountPort.loadAccount(anyString())).thenReturn(Optional.of(createAccount()));

    var account = loadAccountService.loadAccount("email@user.com");
    assertThat(account)
        .extracting(Account::id, Account::email)
        .containsExactly(12345L, "email@user.com");
  }

  @Test
  void shouldThrowAccountNotFoundExceptionWhenAccountNotFound() {
    when(loadAccountPort.loadAccount(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> loadAccountService.loadAccount("email@user.com"))
        .isInstanceOf(AccountNotFoundException.class);
  }

  private Account createAccount() {
    return new Account(12345L, "email@user.com", "summoner-name", "1234", Role.JUNGLE, Region.EUW1);
  }
}
