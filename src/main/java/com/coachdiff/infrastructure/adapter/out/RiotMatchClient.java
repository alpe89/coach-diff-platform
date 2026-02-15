package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.infrastructure.adapter.out.dto.RiotMatchDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotTimelineDTO;
import com.coachdiff.infrastructure.config.RiotProperties;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RiotMatchClient {
  private final RateLimiter rateLimiter;
  private final RestClient riotMatchesClient;

  RiotMatchClient(
      RestClient.Builder restClientBuilder,
      RiotProperties riotProperties,
      RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
    this.riotMatchesClient =
        restClientBuilder.clone().baseUrl(riotProperties.api().baseUrlMatch()).build();
  }

  public List<String> getMatchesIds(String puuid) {
    return rateLimiter.executeSupplier(
        () ->
            riotMatchesClient
                .get()
                .uri("/lol/match/v5/matches/by-puuid/{puuid}/ids?queue=420&start=0&count=20", puuid)
                .retrieve()
                .onStatus(
                    HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
                .body(new ParameterizedTypeReference<>() {}));
  }

  @Cacheable("match-details")
  public RiotMatchDTO getMatchData(String matchId) {
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

  @Cacheable("match-timelines")
  public RiotTimelineDTO getMatchTimelineData(String matchId) {
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
