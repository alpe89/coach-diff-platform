package com.coachdiff.domain.model;

public record MatchAggregate(
    int gamesAnalyzed,
    int wins,
    int losses,
    // Combat
    double avgKills,
    double avgDeaths,
    double avgAssists,
    double avgKda,
    double avgSoloKills,
    double avgDamagePerMinute,
    double avgTotalDamageToChampions,
    double avgDamagePerGold,
    double avgTeamDamagePercentage,
    double avgDamageTakenPercentage,
    double avgKillParticipation,
    // Economy
    double avgGoldPerMinute,
    double avgCsPerMinute,
    double avgLaneMinionsFirst10Min,
    double avgCsAt10,
    double avgGoldAt15,
    // Objectives
    double avgDamageToTurrets,
    double avgDamageToObjectives,
    double avgTurretPlatesTaken,
    // Vision
    double avgVisionScorePerMinute,
    double avgWardsPlaced,
    double avgWardsKilled,
    double avgControlWardsPlaced) {

  public double winRate() {
    if (gamesAnalyzed == 0) return 0.0;
    return (double) wins / gamesAnalyzed;
  }
}
