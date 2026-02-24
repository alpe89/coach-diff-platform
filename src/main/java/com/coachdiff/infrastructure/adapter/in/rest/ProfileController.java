package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.model.Profile;
import com.coachdiff.domain.port.in.FetchProfilePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {
  private final FetchProfilePort fetchProfilePort;

  public ProfileController(FetchProfilePort fetchProfilePort) {
    this.fetchProfilePort = fetchProfilePort;
  }

  @GetMapping("/profile")
  public ResponseEntity<Profile> getProfile(@RequestHeader("X-User-Email") String email) {
    Profile fetchedProfile = fetchProfilePort.getProfile(email);

    return ResponseEntity.ok(fetchedProfile);
  }
}
