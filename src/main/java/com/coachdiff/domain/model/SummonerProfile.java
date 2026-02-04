package com.coachdiff.domain.model;

public record SummonerProfile(
    String name,
    String tag,
    Region region,
    Tier tier,
    Division division,
    int lp,
    int wins,
    int losses) {
  public int gamesPlayed() {
    return wins + losses;
  }

  public Double winRate() {
    if (gamesPlayed() == 0) return 0.0;

    return (double) wins / gamesPlayed();
  }
}
