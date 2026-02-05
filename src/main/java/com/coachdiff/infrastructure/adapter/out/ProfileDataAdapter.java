package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.out.LoadProfileDataPort;
import com.coachdiff.infrastructure.adapter.out.dto.RiotAccountDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotLeagueDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProfileDataAdapter implements LoadProfileDataPort {
  private final RestClient riotAccountsRestClient;
  private final RestClient riotLeagueRestClient;

  public ProfileDataAdapter(
      RestClient.Builder restClientBuilder,
      @Value("${riot.api.base-url-accounts}") String riotAccountsBaseUrl,
      @Value("${riot.api.base-url-league}") String riotLeagueBaseUrl) {
    this.riotAccountsRestClient = restClientBuilder.clone().baseUrl(riotAccountsBaseUrl).build();
    this.riotLeagueRestClient = restClientBuilder.clone().baseUrl(riotLeagueBaseUrl).build();
  }

  @Override
  public Optional<SummonerProfile> loadProfileData(String name, String tag) {

    RiotAccountDTO riotAccount =
        riotAccountsRestClient
            .get()
            .uri("/riot/account/v1/accounts/by-riot-id/{name}/{tag}", name, tag)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {})
            .body(RiotAccountDTO.class);

    return Optional.ofNullable(riotAccount)
        .map(RiotAccountDTO::puuid)
        .flatMap(
            puuid -> {
              List<RiotLeagueDTO> riotLeague =
                  riotLeagueRestClient
                      .get()
                      .uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid)
                      .retrieve()
                      .body(new ParameterizedTypeReference<>() {});

              return Optional.ofNullable(riotLeague)
                  .flatMap(
                      leagueList ->
                          leagueList.stream().filter(RiotLeagueDTO::isSoloQueue).findFirst())
                  .map(
                      league ->
                          new SummonerProfile(
                              name,
                              tag,
                              Region.EUW1,
                              Tier.valueOf(league.tier()),
                              Division.valueOf(league.rank()),
                              league.leaguePoints(),
                              league.wins(),
                              league.losses()));
            });
  }
}
