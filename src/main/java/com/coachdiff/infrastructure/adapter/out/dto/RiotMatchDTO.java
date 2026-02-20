package com.coachdiff.infrastructure.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RiotMatchDTO(Info info) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Info(int gameDuration, List<Participant> participants) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Participant(
      String puuid,
      boolean win,
      String championName,
      String teamPosition,
      // Combat
      int kills,
      int deaths,
      int assists,
      int totalDamageDealtToChampions,
      int goldEarned,
      int damageDealtToTurrets,
      int damageDealtToObjectives,
      // Farming
      int totalMinionsKilled,
      int neutralMinionsKilled,
      // Vision
      int wardsPlaced,
      int wardsKilled,
      // Pre-computed by Riot
      Challenges challenges) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Challenges(
      Double kda,
      Integer soloKills,
      Double damagePerMinute,
      Double teamDamagePercentage,
      Double damageTakenOnTeamPercentage,
      Double killParticipation,
      Double goldPerMinute,
      Double visionScorePerMinute,
      Integer controlWardsPlaced,
      Integer turretPlatesTaken) {

    public double kdaOrZero() {
      return kda != null ? kda : 0.0;
    }

    public int soloKillsOrZero() {
      return soloKills != null ? soloKills : 0;
    }

    public double damagePerMinuteOrZero() {
      return damagePerMinute != null ? damagePerMinute : 0.0;
    }

    public double teamDamagePercentageOrZero() {
      return teamDamagePercentage != null ? teamDamagePercentage : 0.0;
    }

    public double damageTakenOnTeamPercentageOrZero() {
      return damageTakenOnTeamPercentage != null ? damageTakenOnTeamPercentage : 0.0;
    }

    public double killParticipationOrZero() {
      return killParticipation != null ? killParticipation : 0.0;
    }

    public double goldPerMinuteOrZero() {
      return goldPerMinute != null ? goldPerMinute : 0.0;
    }

    public double visionScorePerMinuteOrZero() {
      return visionScorePerMinute != null ? visionScorePerMinute : 0.0;
    }

    public int controlWardsPlacedOrZero() {
      return controlWardsPlaced != null ? controlWardsPlaced : 0;
    }

    public int turretPlatesTakenOrZero() {
      return turretPlatesTaken != null ? turretPlatesTaken : 0;
    }
  }
}
