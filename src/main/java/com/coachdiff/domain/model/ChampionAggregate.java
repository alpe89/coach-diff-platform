package com.coachdiff.domain.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ChampionAggregate(
    String championName,
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

  public static List<ChampionAggregate> fromMatchRecordList(List<Match> matches) {
    Map<String, List<Match>> byChampion =
        matches.stream().collect(Collectors.groupingBy(Match::championName));

    return byChampion.entrySet().stream()
        .map(entry -> buildChampionAggregate(entry.getKey(), entry.getValue()))
        .toList();
  }

  private static ChampionAggregate buildChampionAggregate(
      String championName, List<Match> records) {
    int total = records.size();
    int wins = (int) records.stream().filter(Match::win).count();

    return new ChampionAggregate(
        championName,
        total,
        wins,
        total - wins,
        // Combat
        records.stream().mapToDouble(Match::kills).average().orElse(0),
        records.stream().mapToDouble(Match::deaths).average().orElse(0),
        records.stream().mapToDouble(Match::assists).average().orElse(0),
        records.stream().mapToDouble(Match::kda).average().orElse(0),
        records.stream().mapToDouble(Match::soloKills).average().orElse(0),
        records.stream().mapToDouble(Match::damagePerMinute).average().orElse(0),
        records.stream().mapToDouble(Match::damagePerGold).average().orElse(0),
        records.stream().mapToDouble(Match::teamDamagePercentage).average().orElse(0),
        records.stream().mapToDouble(Match::damageTakenPercentage).average().orElse(0),
        records.stream().mapToDouble(Match::killParticipation).average().orElse(0),
        // Economy
        records.stream().mapToDouble(Match::goldPerMinute).average().orElse(0),
        records.stream().mapToDouble(Match::csPerMinute).average().orElse(0),
        records.stream().mapToDouble(r -> r.csAt10() != null ? r.csAt10() : 0).average().orElse(0),
        records.stream()
            .mapToDouble(r -> r.goldAt10() != null ? r.goldAt10() : 0)
            .average()
            .orElse(0),
        records.stream()
            .mapToDouble(r -> r.goldAt15() != null ? r.goldAt15() : 0)
            .average()
            .orElse(0),
        records.stream().mapToDouble(r -> r.xpAt15() != null ? r.xpAt15() : 0).average().orElse(0),
        // Objectives
        records.stream().mapToDouble(Match::damageToTurrets).average().orElse(0),
        records.stream().mapToDouble(Match::damageToObjectives).average().orElse(0),
        records.stream().mapToDouble(Match::turretPlatesTaken).average().orElse(0),
        // Vision
        records.stream().mapToDouble(Match::visionScorePerMinute).average().orElse(0),
        records.stream().mapToDouble(Match::wardsPlaced).average().orElse(0),
        records.stream().mapToDouble(Match::wardsKilled).average().orElse(0),
        records.stream().mapToDouble(Match::controlWardsPlaced).average().orElse(0));
  }
}
