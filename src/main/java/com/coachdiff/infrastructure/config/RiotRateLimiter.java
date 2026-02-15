package com.coachdiff.infrastructure.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RiotRateLimiter {
  private final RiotProperties riotProps;

  RiotRateLimiter(RiotProperties riotProperties) {
    this.riotProps = riotProperties;
  }

  @Bean
  RateLimiter rateLimiter() {
    return RateLimiter.of(
        "riot-api",
        RateLimiterConfig.custom()
            .limitForPeriod(riotProps.rateLimit().requestsPerSecond())
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofSeconds(riotProps.rateLimit().timeoutSeconds()))
            .build());
  }
}
