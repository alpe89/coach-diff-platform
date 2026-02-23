package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.port.in.FetchAccountPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {
  private final FetchAccountPort fetchAccountPort;

  AccountController(FetchAccountPort fetchAccountPort) {
    this.fetchAccountPort = fetchAccountPort;
  }

  @GetMapping("/account")
  public ResponseEntity<Account> getAccount(@RequestHeader("X-User-Email") String email) {
    // TODO: Will become @AuthenticationPrincipal when implemented
    var account = fetchAccountPort.loadAccount(email);

    return ResponseEntity.ok(account);
  }
}
