package com.coachdiff.application.service;

import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.in.FetchProfilePort;
import org.springframework.stereotype.Service;

@Service
public class FetchProfileService implements FetchProfilePort {
  @Override
  public SummonerProfile getSummonerProfile(String name, String tag) {
    // TODO: Update the hardcoded values to the one coming from RIOT apis.
    return new SummonerProfile(name, tag, Region.EUW1, Tier.GOLD, Division.IV, 10, 8, 3);
  }
}
