package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.infrastructure.adapter.out.dto.RiotLeagueDTO;
import com.coachdiff.infrastructure.config.RiotProperties;
import java.util.List;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RiotLeagueClient {
  private final RestClient client;

  RiotLeagueClient(RestClient.Builder restClientBuilder, RiotProperties riotProperties) {
    this.client = restClientBuilder.clone().baseUrl(riotProperties.api().baseUrlLeague()).build();
  }

  public Optional<RiotLeagueDTO> getRiotLeagueByPuuid(String puuid) {
    List<RiotLeagueDTO> leagues =
        client
            .get()
            .uri("/lol/league/v4/entries/by-puuid/{puuid}", puuid)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
            .body(new ParameterizedTypeReference<>() {});

    return Optional.ofNullable(leagues)
        .flatMap(
            leaguesList -> leaguesList.stream().filter(RiotLeagueDTO::isSoloQueue).findFirst());
  }
}
