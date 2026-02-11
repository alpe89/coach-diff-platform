package com.coachdiff.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "riot")
public record RiotProperties(RiotApi api, RiotDdragon ddragon) {
  public record RiotApi(
      String key,
      String baseUrlAccounts,
      String baseUrlLeague,
      String baseUrlSummoner,
      String baseUrlMatch) {}

  public record RiotDdragon(String baseUrl, String version) {}
}
