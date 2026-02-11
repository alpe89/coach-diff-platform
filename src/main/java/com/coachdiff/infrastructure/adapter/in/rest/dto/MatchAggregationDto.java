package com.coachdiff.infrastructure.adapter.in.rest.dto;

public record MatchAggregationDto(
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
    double avgControlWardsPlaced,
    double winRate) {

  // public static MatchAggregationDto from(MatchAggregate aggregate) {}
}
