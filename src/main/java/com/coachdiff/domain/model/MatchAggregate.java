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
    double avgControlWardsPlaced,
    List<ChampionAggregate> championsAggregate) {

  public double winRate() {
    if (gamesAnalyzed == 0) return 0.0;
    return (double) wins / gamesAnalyzed;
  }

  public static MatchAggregate fromMatchRecordList(List<Match> matches) {
    int total = matches.size();
    if (total == 0) {
      return new MatchAggregate(
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, List.of());
    }

    int wins = (int) matches.stream().filter(Match::win).count();
    List<ChampionAggregate> championsAggregate = ChampionAggregate.fromMatchRecordList(matches);

    return new MatchAggregate(
        total,
        wins,
        total - wins,
        // Combat
        matches.stream().mapToDouble(Match::kills).average().orElse(0),
        matches.stream().mapToDouble(Match::deaths).average().orElse(0),
        matches.stream().mapToDouble(Match::assists).average().orElse(0),
        matches.stream().mapToDouble(Match::kda).average().orElse(0),
        matches.stream().mapToDouble(Match::soloKills).average().orElse(0),
        matches.stream().mapToDouble(Match::damagePerMinute).average().orElse(0),
        matches.stream().mapToDouble(Match::damagePerGold).average().orElse(0),
        matches.stream().mapToDouble(Match::teamDamagePercentage).average().orElse(0),
        matches.stream().mapToDouble(Match::damageTakenPercentage).average().orElse(0),
        matches.stream().mapToDouble(Match::killParticipation).average().orElse(0),
        // Economy
        matches.stream().mapToDouble(Match::goldPerMinute).average().orElse(0),
        matches.stream().mapToDouble(Match::csPerMinute).average().orElse(0),
        matches.stream().mapToDouble(r -> r.csAt10() != null ? r.csAt10() : 0).average().orElse(0),
        matches.stream()
            .mapToDouble(r -> r.goldAt10() != null ? r.goldAt10() : 0)
            .average()
            .orElse(0),
        matches.stream()
            .mapToDouble(r -> r.goldAt15() != null ? r.goldAt15() : 0)
            .average()
            .orElse(0),
        matches.stream().mapToDouble(r -> r.xpAt15() != null ? r.xpAt15() : 0).average().orElse(0),
        // Objectives
        matches.stream().mapToDouble(Match::damageToTurrets).average().orElse(0),
        matches.stream().mapToDouble(Match::damageToObjectives).average().orElse(0),
        matches.stream().mapToDouble(Match::turretPlatesTaken).average().orElse(0),
        // Vision
        matches.stream().mapToDouble(Match::visionScorePerMinute).average().orElse(0),
        matches.stream().mapToDouble(Match::wardsPlaced).average().orElse(0),
        matches.stream().mapToDouble(Match::wardsKilled).average().orElse(0),
        matches.stream().mapToDouble(Match::controlWardsPlaced).average().orElse(0),
        championsAggregate);
  }
}
