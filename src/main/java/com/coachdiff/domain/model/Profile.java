package com.coachdiff.domain.model;

public record Profile(
    String name,
    String tag,
    Region region,
    String profileIconURI,
    Tier tier,
    Division division,
    int lp,
    int wins,
    int losses,
    int gamesPlayed,
    Double winRate) {

  public static Profile composeProfile(String name, String tag, Summoner summoner, Rank rank) {
    var gamesPlayed = rank.wins() + rank.losses();
    var winRate = gamesPlayed == 0 ? 0.0 : (double) rank.wins() / gamesPlayed;

    return new Profile(
        name,
        tag,
        Region.EUW1, // Fixed for now
        summoner.profileIconURI(),
        rank.tier(),
        rank.division(),
        rank.lp(),
        rank.wins(),
        rank.losses(),
        gamesPlayed,
        winRate);
  }
}
