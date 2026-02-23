package com.coachdiff.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coachdiff.domain.exception.AccountNotFoundException;
import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.port.in.FetchAccountPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private FetchAccountPort fetchAccountPort;

  @Test
  void shouldReturnAccount() throws Exception {
    when(fetchAccountPort.loadAccount(anyString()))
        .thenReturn(
            new Account(1L, "test@example.com", "TestName", "TestTag", Role.ADC, Region.EUW1));

    mockMvc
        .perform(get("/api/account").header("X-User-Email", "test@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.name").value("TestName"))
        .andExpect(jsonPath("$.tag").value("TestTag"))
        .andExpect(jsonPath("$.role").value("ADC"))
        .andExpect(jsonPath("$.region").value("EUW1"));
  }

  @Test
  void shouldReturnNotFoundWhenAccountNotFound() throws Exception {
    when(fetchAccountPort.loadAccount(any()))
        .thenThrow(
            new AccountNotFoundException(ErrorCode.ACCOUNT_DATA_NOT_FOUND, "Account not found"));

    mockMvc
        .perform(get("/api/account").header("X-User-Email", "test@example.com"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_DATA_NOT_FOUND.name()))
        .andExpect(jsonPath("$.message").value("Account not found"));
  }
}
