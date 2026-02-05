package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.port.in.FetchProfilePort;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<SummonerProfile> getProfile() {
    // TODO: Update the hardcoded summoner profile values with the ones in the configs.
    Optional<SummonerProfile> fetchedProfile = fetchProfilePort.getSummonerProfile("Alpe", "#1989");

    return fetchedProfile
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
