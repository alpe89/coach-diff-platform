package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.domain.model.Summoner;
import com.coachdiff.domain.port.out.FetchSummonerDataPort;
import com.coachdiff.infrastructure.adapter.out.dto.RiotSummonerDTO;
import com.coachdiff.infrastructure.config.RiotProperties;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RiotSummonerDataAdapter implements FetchSummonerDataPort {
  private final RiotSummonerClient client;
  private final RiotProperties.RiotDdragon riotDdragon;

  RiotSummonerDataAdapter(RiotSummonerClient client, RiotProperties riotProperties) {
    this.client = client;
    this.riotDdragon = riotProperties.ddragon();
  }

  @Override
  public Optional<Summoner> getSummonerDataByPuuid(String puuid) {
    return client.getRiotSummonerByPuuid(puuid).map(this::createSummonerRecord);
  }

  private Summoner createSummonerRecord(RiotSummonerDTO dto) {
    var profileIconURI =
        this.riotDdragon.baseUrl()
            + "/"
            + this.riotDdragon.version()
            + "/img/profileicon/"
            + dto.profileIconId()
            + ".png";

    return new Summoner(profileIconURI);
  }
}
