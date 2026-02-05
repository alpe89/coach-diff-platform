package com.coachdiff.infrastructure.adapter.out.dto;

public record RiotLeagueDTO(
    String leagueId,
    String queueType,
    String tier,
    String rank,
    String puuid,
    int leaguePoints,
    int wins,
    int losses,
    boolean veteran,
    boolean inactive,
    boolean freshBlood,
    boolean hotStreak) {

  public static final String RANKED_SOLO_QUEUE = "RANKED_SOLO_5x5";

  public boolean isSoloQueue() {
    return RANKED_SOLO_QUEUE.equals(queueType);
  }
}
