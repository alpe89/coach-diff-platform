package com.coachdiff.application.service;

import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.port.in.FetchProfilePort;
import com.coachdiff.domain.port.out.LoadProfileDataPort;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FetchProfileService implements FetchProfilePort {
  private final LoadProfileDataPort loadProfileDataPort;

  FetchProfileService(LoadProfileDataPort loadProfileDataPort) {
    this.loadProfileDataPort = loadProfileDataPort;
  }

  @Override
  public Optional<SummonerProfile> getSummonerProfile(String name, String tag) {
    return loadProfileDataPort.loadProfileData(name, tag);
  }
}
