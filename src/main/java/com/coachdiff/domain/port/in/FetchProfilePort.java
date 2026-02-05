package com.coachdiff.domain.port.in;

import com.coachdiff.domain.model.SummonerProfile;
import java.util.Optional;

public interface FetchProfilePort {
  Optional<SummonerProfile> getSummonerProfile(String name, String tag);
}
