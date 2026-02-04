package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.port.in.FetchProfilePort;
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
  public SummonerProfile getProfile() {
    // TODO: Update the hardcoded summoner profile values with the ones in the configs.
    return fetchProfilePort.getSummonerProfile("Alpe", "#1989");
  }
}
