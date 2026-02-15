package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.out.LoadProfileDataPort;
import com.coachdiff.infrastructure.adapter.out.dto.RiotLeagueDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotSummonerDTO;
import com.coachdiff.infrastructure.config.RiotProperties;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProfileDataAdapter implements LoadProfileDataPort {
  private static final Logger log = LoggerFactory.getLogger(ProfileDataAdapter.class);
  private final RestClient riotLeagueRestClient;
  private final RestClient riotSummonerRestClient;
  private final String riotDdragonBaseUrl;
  private final String riotDdragonVersion;
  private final RiotAccountClient riotAccountClient;

  public ProfileDataAdapter(
      RestClient.Builder restClientBuilder,
      RiotProperties riotProperties,
      RiotAccountClient riotAccountClient) {
    this.riotLeagueRestClient =
        restClientBuilder.clone().baseUrl(riotProperties.api().baseUrlLeague()).build();
    this.riotSummonerRestClient =
        restClientBuilder.clone().baseUrl(riotProperties.api().baseUrlSummoner()).build();
    this.riotDdragonBaseUrl = riotProperties.ddragon().baseUrl();
    this.riotDdragonVersion = riotProperties.ddragon().version();
    this.riotAccountClient = riotAccountClient;
  }

  @Override
  public Optional<SummonerProfile> loadProfileData(String name, String tag) {

    return riotAccountClient
        .getRiotAccountPuuid(name, tag)
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
                log.error("Thread interrupted while fetching profile data", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
              } catch (StructuredTaskScope.FailedException e) {

                if (e.getCause() instanceof RuntimeException re) {
                  throw re;
                }

                throw new RuntimeException(e.getCause());
              }
            });
  }

  private Optional<RiotLeagueDTO> getSoloQueueData(String puuid) {
    List<RiotLeagueDTO> leagues =
        riotLeagueRestClient
            .get()
            .uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
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
            .onStatus(HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
            .body(RiotSummonerDTO.class));
  }

  private SummonerProfile composeSummonerProfileData(
      String name, String tag, RiotSummonerDTO summonerData, RiotLeagueDTO leagueData) {
    String profileURI =
        this.riotDdragonBaseUrl
            + "/"
            + this.riotDdragonVersion
            + "/img/profileicon/"
            + summonerData.profileIconId()
            + ".png";

    return new SummonerProfile(
        name,
        tag,
        Region.EUW1,
        profileURI,
        Tier.valueOf(leagueData.tier()),
        Division.valueOf(leagueData.rank()),
        leagueData.leaguePoints(),
        leagueData.wins(),
        leagueData.losses());
  }
}
