package com.coachdiff.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.coachdiff.domain.exception.AccountNotFoundException;
import com.coachdiff.domain.exception.BenchmarkNotFoundException;
import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotAuthenticationException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotRateLimitException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotUnknownException;
import org.junit.jupiter.api.Test;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldHandleDomainNotFoundException() {
    var ex =
        new SummonerProfileNotFoundException(ErrorCode.SUMMONER_NOT_FOUND, "Profile not found");

    var response = handler.domainNotFoundHandler(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(response.getBody().code()).isEqualTo(ErrorCode.SUMMONER_NOT_FOUND.name());
    assertThat(response.getBody().message()).isEqualTo("Profile not found");
  }

  @Test
  void shouldHandleAccountNotFoundException() {
    var ex = new AccountNotFoundException(ErrorCode.ACCOUNT_DATA_NOT_FOUND, "Account not found");

    var response = handler.domainNotFoundHandler(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(response.getBody().code()).isEqualTo(ErrorCode.ACCOUNT_DATA_NOT_FOUND.name());
  }

  @Test
  void shouldHandleBenchmarkNotFoundException() {
    var ex =
        new BenchmarkNotFoundException(ErrorCode.BENCHMARK_DATA_NOT_FOUND, "Benchmark not found");

    var response = handler.domainNotFoundHandler(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(response.getBody().code()).isEqualTo(ErrorCode.BENCHMARK_DATA_NOT_FOUND.name());
  }

  @Test
  void shouldHandleRiotAuthenticationException() {
    var ex = new RiotAuthenticationException(401, "Unauthorized");

    var response = handler.riotExceptionHandler(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(401);
    assertThat(response.getBody().code()).isEqualTo(ErrorCode.RIOT_API_ERROR.name());
    assertThat(response.getBody().message()).isEqualTo("Unauthorized");
  }

  @Test
  void shouldHandleRiotRateLimitException() {
    var ex = new RiotRateLimitException(429, "Rate limited");

    var response = handler.riotExceptionHandler(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(429);
    assertThat(response.getBody().code()).isEqualTo(ErrorCode.RIOT_API_RATE_LIMIT.name());
    assertThat(response.getBody().message()).isEqualTo("Rate limited");
  }

  @Test
  void shouldHandleRiotUnknownException() {
    var ex = new RiotUnknownException(500, "Internal server error");

    var response = handler.riotExceptionHandler(ex);

    assertThat(response.getStatusCode().value()).isEqualTo(500);
    assertThat(response.getBody().code()).isEqualTo(ErrorCode.UNEXPECTED_ERROR.name());
    assertThat(response.getBody().message()).isEqualTo("Internal server error");
  }
}
