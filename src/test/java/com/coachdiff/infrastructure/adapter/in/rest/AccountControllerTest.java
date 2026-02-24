package com.coachdiff.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coachdiff.domain.exception.AccountNotFoundException;
import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.port.in.ManageAccountPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private ManageAccountPort manageAccountPort;

  @Test
  void shouldReturnAccount() throws Exception {
    when(manageAccountPort.loadAccount(anyString()))
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
    when(manageAccountPort.loadAccount(any()))
        .thenThrow(
            new AccountNotFoundException(ErrorCode.ACCOUNT_DATA_NOT_FOUND, "Account not found"));

    mockMvc
        .perform(get("/api/account").header("X-User-Email", "test@example.com"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_DATA_NOT_FOUND.name()))
        .andExpect(jsonPath("$.message").value("Account not found"));
  }

  @Test
  void shouldCreateAccount() throws Exception {
    when(manageAccountPort.saveAccount(any()))
        .thenReturn(
            new Account(1L, "test@example.com", "TestName", "TestTag", Role.ADC, Region.EUW1));

    mockMvc
        .perform(
            post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "test@example.com",
                      "name": "TestName",
                      "tag": "TestTag",
                      "role": "ADC",
                      "region": "EUW1"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.name").value("TestName"))
        .andExpect(jsonPath("$.tag").value("TestTag"))
        .andExpect(jsonPath("$.role").value("ADC"))
        .andExpect(jsonPath("$.region").value("EUW1"));
  }

  @Test
  void shouldUpdateAccount() throws Exception {
    when(manageAccountPort.loadAccount("email@user.com"))
        .thenReturn(
            new Account(1L, "email@user.com", "TestName", "TestTag", Role.ADC, Region.EUW1));

    mockMvc
        .perform(
            patch("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Raiden",
                      "tag": "Bzzt",
                      "role": "TOP",
                      "region": "EUW1"
                    }
                    """)
                .header("X-User-Email", "email@user.com"))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldDeleteAccount() throws Exception {
    when(manageAccountPort.loadAccount("email@user.com"))
        .thenReturn(
            new Account(1L, "email@user.com", "TestName", "TestTag", Role.ADC, Region.EUW1));

    mockMvc
        .perform(delete("/api/account").header("X-User-Email", "email@user.com"))
        .andExpect(status().isNoContent());
  }
}
