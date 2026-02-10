package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.out.LoadProfileDataPort;
import com.coachdiff.infrastructure.adapter.out.dto.RiotAccountDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotLeagueDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotSummonerDTO;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProfileDataAdapter implements LoadProfileDataPort {
  private final RestClient riotAccountsRestClient;
  private final RestClient riotLeagueRestClient;
  private final RestClient riotSummonerRestClient;
  private final String riotDdragonBaseUrl;
  private final String riotDdragonVersion;

  public ProfileDataAdapter(
      RestClient.Builder restClientBuilder,
      @Value("${riot.api.base-url-accounts}") String riotAccountsBaseUrl,
      @Value("${riot.api.base-url-league}") String riotLeagueBaseUrl,
      @Value("${riot.api.base-url-summoner}") String riotSummonerBaseUrl,
      @Value("${riot.ddragon.base-url}") String riotDdragonBaseUrl,
      @Value("${riot.ddragon.version}") String riotDdragonVersion) {
    this.riotAccountsRestClient = restClientBuilder.clone().baseUrl(riotAccountsBaseUrl).build();
    this.riotLeagueRestClient = restClientBuilder.clone().baseUrl(riotLeagueBaseUrl).build();
    this.riotSummonerRestClient = restClientBuilder.clone().baseUrl(riotSummonerBaseUrl).build();
    this.riotDdragonBaseUrl = riotDdragonBaseUrl;
    this.riotDdragonVersion = riotDdragonVersion;
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
              try (var scope = StructuredTaskScope.open()) {
                StructuredTaskScope.Subtask<Optional<RiotLeagueDTO>> leagueTask =
                    scope.fork(() -> getSoloQueueData(puuid));
                StructuredTaskScope.Subtask<Optional<RiotSummonerDTO>> summonerTask =
                    scope.fork(() -> getSummonerData(puuid));

                scope.join();

                Optional<RiotLeagueDTO> leagueData = leagueTask.get();
                Optional<RiotSummonerDTO> summonerData = summonerTask.get();

                return leagueData.flatMap(
                    league ->
                        summonerData.map(
                            summoner -> composeSummonerProfileData(name, tag, summoner, league)));
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
              }
            });
  }

  private Optional<RiotLeagueDTO> getSoloQueueData(String puuid) {
    List<RiotLeagueDTO> leagues =
        riotLeagueRestClient
            .get()
            .uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

    return Optional.ofNullable(leagues)
        .flatMap(
            leaguesList -> leaguesList.stream().filter(RiotLeagueDTO::isSoloQueue).findFirst());
  }

  private Optional<RiotSummonerDTO> getSummonerData(String puuid) {
    return Optional.ofNullable(
        riotSummonerRestClient
            .get()
            .uri("/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {})
            .body(RiotSummonerDTO.class));
  }

  private SummonerProfile composeSummonerProfileData(
      String name, String tag, RiotSummonerDTO summonerData, RiotLeagueDTO leagueData) {
    return new SummonerProfile(
        name,
        tag,
        Region.EUW1,
        this.riotDdragonBaseUrl
            + "/"
            + this.riotDdragonVersion
            + "/img/profileicon/"
            + summonerData.profileIconId()
            + ".png",
        Tier.valueOf(leagueData.tier()),
        Division.valueOf(leagueData.rank()),
        leagueData.leaguePoints(),
        leagueData.wins(),
        leagueData.losses());
  }
}
