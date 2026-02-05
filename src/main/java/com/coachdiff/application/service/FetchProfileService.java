package com.coachdiff.application.service;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.port.in.FetchProfilePort;
import com.coachdiff.domain.port.out.LoadProfileDataPort;
import org.springframework.stereotype.Service;

@Service
public class FetchProfileService implements FetchProfilePort {
  private final LoadProfileDataPort loadProfileDataPort;

  FetchProfileService(LoadProfileDataPort loadProfileDataPort) {
    this.loadProfileDataPort = loadProfileDataPort;
  }

  @Override
  public SummonerProfile getSummonerProfile(String name, String tag) {
    return loadProfileDataPort
        .loadProfileData(name, tag)
        .orElseThrow(
            () ->
                new SummonerProfileNotFoundException(
                    ErrorCode.SUMMONER_NOT_FOUND, "Profile not found for " + name + "#" + tag));
  }
}
