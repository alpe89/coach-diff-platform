package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.port.in.FetchProfilePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {
  private final FetchProfilePort fetchProfilePort;

  @Value("${coach-diff.default-summoner.name}")
  private String summonerName;

  @Value("${coach-diff.default-summoner.tag}")
  private String summonerTag;

  public ProfileController(FetchProfilePort fetchProfilePort) {
    this.fetchProfilePort = fetchProfilePort;
  }

  @GetMapping("/profile")
  public ResponseEntity<SummonerProfile> getProfile() {
    SummonerProfile fetchedProfile = fetchProfilePort.getSummonerProfile(summonerName, summonerTag);

    return ResponseEntity.ok(fetchedProfile);
  }
}
