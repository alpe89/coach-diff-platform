package com.coachdiff.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SummonerProfile(
    String name,
    String tag,
    Region region,
    String profileIconURI,
    Tier tier,
    Division division,
    int lp,
    int wins,
    int losses) {
  @JsonProperty("gamesPlayed")
  public int gamesPlayed() {
    return wins + losses;
  }

  @JsonProperty("winRate")
  public Double winRate() {
    if (gamesPlayed() == 0) return 0.0;

    return (double) wins / gamesPlayed();
  }
}
