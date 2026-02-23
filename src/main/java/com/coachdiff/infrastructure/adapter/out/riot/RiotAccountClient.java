package com.coachdiff.infrastructure.adapter.out.riot;

import com.coachdiff.infrastructure.adapter.out.dto.RiotAccountDTO;
import com.coachdiff.infrastructure.config.RiotProperties;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RiotAccountClient {
  private static final Logger log = LoggerFactory.getLogger(RiotAccountClient.class);
  private final RestClient riotAccountsRestClient;
  private final RateLimiter rateLimiter;

  RiotAccountClient(
      RestClient.Builder restClientBuilder,
      RiotProperties riotProperties,
      RateLimiter rateLimiter) {
    this.riotAccountsRestClient =
        restClientBuilder.clone().baseUrl(riotProperties.api().baseUrlAccounts()).build();
    this.rateLimiter = rateLimiter;
  }

  @Cacheable("account-details")
  public Optional<String> getRiotAccountPuuid(String name, String tag) {
    log.debug("Fetching PUUID for {}#{}", name, tag);
    var riotAccount =
        rateLimiter.executeSupplier(
            () ->
                riotAccountsRestClient
                    .get()
                    .uri("/riot/account/v1/accounts/by-riot-id/{name}/{tag}", name, tag)
                    .retrieve()
                    .onStatus(
                        HttpStatusCode::is4xxClientError, RiotExceptionHandler::handleRiotException)
                    .body(RiotAccountDTO.class));

    return Optional.ofNullable(riotAccount).map(RiotAccountDTO::puuid);
  }
}
