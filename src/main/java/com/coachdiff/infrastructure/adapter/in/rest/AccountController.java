package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.port.in.ManageAccountPort;
import com.coachdiff.infrastructure.adapter.in.rest.dto.UpdateAccountRequestDto;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {
  private final ManageAccountPort manageAccountPort;

  AccountController(ManageAccountPort manageAccountPort) {
    this.manageAccountPort = manageAccountPort;
  }

  @GetMapping("/account")
  public ResponseEntity<Account> getAccount(@RequestHeader("X-User-Email") String email) {
    // TODO: Will become @AuthenticationPrincipal when implemented
    var account = manageAccountPort.loadAccount(email);

    return ResponseEntity.ok(account);
  }

  @PostMapping("/account")
  public ResponseEntity<Account> createAccount(@RequestBody Account account) {
    var createdAccount = manageAccountPort.saveAccount(account);
    return ResponseEntity.created(URI.create("api/account/")).body(createdAccount);
  }

  @PatchMapping("/account")
  public ResponseEntity<Void> updateAccount(
      @RequestHeader("X-User-Email") String email, @RequestBody UpdateAccountRequestDto body) {
    manageAccountPort.updateAccount(UpdateAccountRequestDto.toAccount(null, email, body));

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/account")
  public ResponseEntity<Void> deleteAccount(@RequestHeader("X-User-Email") String email) {
    manageAccountPort.deleteAccount(email);

    return ResponseEntity.noContent().build();
  }
}
