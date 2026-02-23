package com.coachdiff.infrastructure.adapter.out.riot;

import com.coachdiff.infrastructure.adapter.out.exception.RiotAuthenticationException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotRateLimitException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotUnknownException;
import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

public class RiotExceptionHandler {
  public static void handleRiotException(HttpRequest request, ClientHttpResponse response)
      throws IOException {
    switch (response.getStatusCode().value()) {
      // 404 status is being handled from the adapter with a custom Exception
      case 404 -> {}
      case 403 ->
          throw new RiotAuthenticationException(
              403, "Encountered a problem likely with the Riot API key");
      case 429 ->
          throw new RiotRateLimitException(429, "Encountered a rate limit issue with the Riot API");
      default -> throw new RiotUnknownException(500, "Unknown error on the Riot API");
    }
  }
}
