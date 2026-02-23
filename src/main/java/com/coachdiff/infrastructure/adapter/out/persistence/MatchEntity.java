package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.Match;
import com.coachdiff.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "match_data")
@IdClass(MatchId.class)
public class MatchEntity {

  // Identity
  @Id private String matchId;
  @Id private String puuid;

  // Game info
  private boolean win;
  private double gameDurationMinutes;
  private String championName;
  private String role;

  // Combat
  private int kills;
  private int deaths;
  private int assists;
  private double kda;
  private int soloKills;
  private double damagePerMinute;
  private double damagePerGold;
  private double teamDamagePercentage;
  private double damageTakenPercentage;
  private double killParticipation;

  // Economy
  private double goldPerMinute;
  private double csPerMinute;

  // Objectives
  private int damageToTurrets;
  private int damageToObjectives;
  private int turretPlatesTaken;

  // Vision
  private double visionScorePerMinute;
  private int wardsPlaced;
  private int wardsKilled;
  private int controlWardsPlaced;

  // Timeline (nullable — not all games reach 10/15 min)
  @Column private Double csAt10;
  @Column private Double goldAt10;
  @Column private Double goldAt15;
  @Column private Double xpAt15;

  protected MatchEntity() {}

  public MatchEntity(
      String matchId,
      String puuid,
      boolean win,
      double gameDurationMinutes,
      String championName,
      String role,
      int kills,
      int deaths,
      int assists,
      double kda,
      int soloKills,
      double damagePerMinute,
      double damagePerGold,
      double teamDamagePercentage,
      double damageTakenPercentage,
      double killParticipation,
      double goldPerMinute,
      double csPerMinute,
      int damageToTurrets,
      int damageToObjectives,
      int turretPlatesTaken,
      double visionScorePerMinute,
      int wardsPlaced,
      int wardsKilled,
      int controlWardsPlaced,
      Double csAt10,
      Double goldAt10,
      Double goldAt15,
      Double xpAt15) {
    this.matchId = matchId;
    this.puuid = puuid;
    this.win = win;
    this.gameDurationMinutes = gameDurationMinutes;
    this.championName = championName;
    this.role = role;
    this.kills = kills;
    this.deaths = deaths;
    this.assists = assists;
    this.kda = kda;
    this.soloKills = soloKills;
    this.damagePerMinute = damagePerMinute;
    this.damagePerGold = damagePerGold;
    this.teamDamagePercentage = teamDamagePercentage;
    this.damageTakenPercentage = damageTakenPercentage;
    this.killParticipation = killParticipation;
    this.goldPerMinute = goldPerMinute;
    this.csPerMinute = csPerMinute;
    this.damageToTurrets = damageToTurrets;
    this.damageToObjectives = damageToObjectives;
    this.turretPlatesTaken = turretPlatesTaken;
    this.visionScorePerMinute = visionScorePerMinute;
    this.wardsPlaced = wardsPlaced;
    this.wardsKilled = wardsKilled;
    this.controlWardsPlaced = controlWardsPlaced;
    this.csAt10 = csAt10;
    this.goldAt10 = goldAt10;
    this.goldAt15 = goldAt15;
    this.xpAt15 = xpAt15;
  }

  public MatchId getId() {
    return new MatchId(matchId, puuid);
  }

  public double getKda() {
    return kda;
  }

  public int getSoloKills() {
    return soloKills;
  }

  public double getDamagePerMinute() {
    return damagePerMinute;
  }

  public int getGoldAt10() {
    return goldAt10 != null ? goldAt10.intValue() : 0;
  }

  public String getChampionName() {
    return championName;
  }

  public Role getRole() {
    return Role.valueOf(role);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MatchEntity other)) return false;
    return Objects.equals(matchId, other.matchId) && Objects.equals(puuid, other.puuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(matchId, puuid);
  }

  public static MatchEntity from(Match record) {
    return new MatchEntity(
        record.matchId(),
        record.puuid(),
        record.win(),
        record.gameDurationMinutes(),
        record.championName(),
        record.role().name(),
        record.kills(),
        record.deaths(),
        record.assists(),
        record.kda(),
        record.soloKills(),
        record.damagePerMinute(),
        record.damagePerGold(),
        record.teamDamagePercentage(),
        record.damageTakenPercentage(),
        record.killParticipation(),
        record.goldPerMinute(),
        record.csPerMinute(),
        record.damageToTurrets(),
        record.damageToObjectives(),
        record.turretPlatesTaken(),
        record.visionScorePerMinute(),
        record.wardsPlaced(),
        record.wardsKilled(),
        record.controlWardsPlaced(),
        record.csAt10(),
        record.goldAt10(),
        record.goldAt15(),
        record.xpAt15());
  }

  public static Match toDomain(MatchEntity entity) {
    return new Match(
        entity.matchId,
        entity.puuid,
        entity.win,
        entity.gameDurationMinutes,
        entity.championName,
        Role.valueOf(entity.role),
        entity.kills,
        entity.deaths,
        entity.assists,
        entity.kda,
        entity.soloKills,
        entity.damagePerMinute,
        entity.damagePerGold,
        entity.teamDamagePercentage,
        entity.damageTakenPercentage,
        entity.killParticipation,
        entity.goldPerMinute,
        entity.csPerMinute,
        entity.damageToTurrets,
        entity.damageToObjectives,
        entity.turretPlatesTaken,
        entity.visionScorePerMinute,
        entity.wardsPlaced,
        entity.wardsKilled,
        entity.controlWardsPlaced,
        entity.csAt10,
        entity.goldAt10,
        entity.goldAt15,
        entity.xpAt15);
  }
}
