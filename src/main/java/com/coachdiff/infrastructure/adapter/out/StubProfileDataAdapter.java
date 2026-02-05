package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.out.LoadProfileDataPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class StubProfileDataAdapter implements LoadProfileDataPort {

  @Override
  public Optional<SummonerProfile> loadProfileData(String name, String tag) {
    return Optional.of(
        new SummonerProfile(name, tag, Region.EUW1, Tier.EMERALD, Division.I, 36, 5, 5));
  }
}
