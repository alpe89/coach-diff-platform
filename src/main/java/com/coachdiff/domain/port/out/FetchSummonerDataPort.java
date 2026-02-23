package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.Summoner;
import java.util.Optional;

public interface FetchSummonerDataPort {
  Optional<Summoner> getSummonerDataByPuuid(String puuid);
}
