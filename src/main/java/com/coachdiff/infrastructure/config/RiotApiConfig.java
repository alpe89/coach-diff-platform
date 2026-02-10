package com.coachdiff.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(RiotProperties.class)
public class RiotApiConfig {
  @Value("${riot.api.key}")
  private String riotApiKey;

  @Bean
  RestClient.Builder riotRestClient() {
    return RestClient.builder().defaultHeader("X-Riot-Token", riotApiKey);
  }
}
