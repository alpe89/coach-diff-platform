package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.SummonerProfile;
import java.util.Optional;

public interface LoadProfileDataPort {
  Optional<SummonerProfile> loadProfileData(String name, String tag);
}
