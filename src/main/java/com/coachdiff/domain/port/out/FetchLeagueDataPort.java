package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.RankRecord;
import java.util.Optional;

public interface FetchLeagueDataPort {
  Optional<RankRecord> getLeagueDataByPuuid(String puuid);
}
