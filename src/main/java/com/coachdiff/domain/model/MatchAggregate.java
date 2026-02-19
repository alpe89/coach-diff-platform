package com.coachdiff.domain.model;

import java.util.List;

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
    double avgDamagePerGold,
    double avgTeamDamagePercentage,
    double avgDamageTakenPercentage,
    double avgKillParticipation,
    // Economy
    double avgGoldPerMinute,
    double avgCsPerMinute,
    double avgCsAt10,
    double avgGoldAt10,
    double avgGoldAt15,
    double avgXpAt15,
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

  public static MatchAggregate fromMatchRecordList(List<MatchRecord> matchRecords) {
    int total = matchRecords.size();
    if (total == 0) {
      return new MatchAggregate(
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    int wins = (int) matchRecords.stream().filter(MatchRecord::win).count();

    return new MatchAggregate(
        total,
        wins,
        total - wins,
        // Combat
        matchRecords.stream().mapToDouble(MatchRecord::kills).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::deaths).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::assists).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::kda).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::soloKills).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::damagePerMinute).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::damagePerGold).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::teamDamagePercentage).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::damageTakenPercentage).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::killParticipation).average().orElse(0),
        // Economy
        matchRecords.stream().mapToDouble(MatchRecord::goldPerMinute).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::csPerMinute).average().orElse(0),
        matchRecords.stream()
            .mapToDouble(r -> r.csAt10() != null ? r.csAt10() : 0)
            .average()
            .orElse(0),
        matchRecords.stream()
            .mapToDouble(r -> r.goldAt10() != null ? r.goldAt10() : 0)
            .average()
            .orElse(0),
        matchRecords.stream()
            .mapToDouble(r -> r.goldAt15() != null ? r.goldAt15() : 0)
            .average()
            .orElse(0),
        matchRecords.stream()
            .mapToDouble(r -> r.xpAt15() != null ? r.xpAt15() : 0)
            .average()
            .orElse(0),
        // Objectives
        matchRecords.stream().mapToDouble(MatchRecord::damageToTurrets).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::damageToObjectives).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::turretPlatesTaken).average().orElse(0),
        // Vision
        matchRecords.stream().mapToDouble(MatchRecord::visionScorePerMinute).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::wardsPlaced).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::wardsKilled).average().orElse(0),
        matchRecords.stream().mapToDouble(MatchRecord::controlWardsPlaced).average().orElse(0));
  }
}
