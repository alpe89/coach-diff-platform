package com.coachdiff.infrastructure.adapter.out.riot;

import com.coachdiff.infrastructure.adapter.out.dto.RiotSummonerDTO;
import com.coachdiff.infrastructure.config.RiotProperties;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RiotSummonerClient {
  private static final Logger log = LoggerFactory.getLogger(RiotSummonerClient.class);
  private final RestClient client;

  RiotSummonerClient(RestClient.Builder restClientBuilder, RiotProperties riotProperties) {
    this.client = restClientBuilder.clone().baseUrl(riotProperties.api().baseUrlSummoner()).build();
  }

  public Optional<RiotSummonerDTO> getRiotSummonerByPuuid(String puuid) {
    log.debug("Fetching summoner data for puuid={}", puuid);
    return Optional.ofNullable(
        client
            .get()
            .uri("/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
            .body(RiotSummonerDTO.class));
  }
}
