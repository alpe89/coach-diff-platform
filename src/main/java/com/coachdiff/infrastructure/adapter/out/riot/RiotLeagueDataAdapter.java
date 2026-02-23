package com.coachdiff.infrastructure.adapter.out.riot;

import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Rank;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.out.FetchLeagueDataPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RiotLeagueDataAdapter implements FetchLeagueDataPort {
  private final RiotLeagueClient client;

  RiotLeagueDataAdapter(RiotLeagueClient client) {
    this.client = client;
  }

  @Override
  public Optional<Rank> getLeagueDataByPuuid(String puuid) {
    var leagueData = client.getRiotLeagueByPuuid(puuid);

    return leagueData.map(
        dto ->
            new Rank(
                Tier.valueOf(dto.tier()),
                Division.valueOf(dto.rank()),
                dto.leaguePoints(),
                dto.wins(),
                dto.losses()));
  }
}
