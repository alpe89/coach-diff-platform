package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.Rank;
import java.util.Optional;

public interface FetchLeagueDataPort {
  Optional<Rank> getLeagueDataByPuuid(String puuid);
}
