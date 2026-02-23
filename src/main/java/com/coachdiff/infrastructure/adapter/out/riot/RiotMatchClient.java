package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.infrastructure.adapter.out.dto.RiotMatchDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotTimelineDTO;
import com.coachdiff.infrastructure.config.RiotProperties;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RiotMatchClient {
  private static final Logger log = LoggerFactory.getLogger(RiotMatchClient.class);
  private final RateLimiter rateLimiter;
  private final RestClient riotMatchesClient;
  private final long seasonStartEpoch;

  RiotMatchClient(
      RestClient.Builder restClientBuilder,
      RiotProperties riotProperties,
      RateLimiter rateLimiter,
      @Value("${coach-diff.season-start-epoch}") long seasonStartEpoch) {
    this.rateLimiter = rateLimiter;
    this.seasonStartEpoch = seasonStartEpoch;
    this.riotMatchesClient =
        restClientBuilder.clone().baseUrl(riotProperties.api().baseUrlMatch()).build();
  }

  public List<String> getMatchesIds(String puuid) {
    log.debug("Fetching match IDs for puuid={}, seasonStart={}", puuid, seasonStartEpoch);
    List<String> result =
        rateLimiter.executeSupplier(
            () ->
                riotMatchesClient
                    .get()
                    .uri(
                        "/lol/match/v5/matches/by-puuid/{puuid}/ids?queue=420&start=0&count=20&startTime={startTime}",
                        puuid,
                        seasonStartEpoch)
                    .retrieve()
                    .onStatus(
                        HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
                    .body(new ParameterizedTypeReference<>() {}));
    log.debug("Found {} match IDs for puuid={}", result != null ? result.size() : 0, puuid);
    return result;
  }

  public RiotMatchDTO getMatchData(String matchId) {
    log.debug("Fetching match details for matchId={}", matchId);
    return rateLimiter.executeSupplier(
        () ->
            riotMatchesClient
                .get()
                .uri("/lol/match/v5/matches/{matchId}", matchId)
                .retrieve()
                .onStatus(
                    HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
                .body(RiotMatchDTO.class));
  }

  public RiotTimelineDTO getMatchTimelineData(String matchId) {
    log.debug("Fetching timeline for matchId={}", matchId);
    return rateLimiter.executeSupplier(
        () ->
            riotMatchesClient
                .get()
                .uri("/lol/match/v5/matches/{matchId}/timeline", matchId)
                .retrieve()
                .onStatus(
                    HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
                .body(RiotTimelineDTO.class));
  }
}
