package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.SummonerRecord;
import java.util.Optional;

public interface FetchSummonerDataPort {
  Optional<SummonerRecord> getSummonerDataByPuuid(String puuid);
}
