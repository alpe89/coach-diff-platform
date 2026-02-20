package com.coachdiff.testutil;

import com.coachdiff.domain.model.MatchRecord;
import com.coachdiff.domain.model.Role;
import com.coachdiff.infrastructure.adapter.out.persistence.MatchRecordEntity;

public final class TestFixtures {

  private TestFixtures() {}

  public static MatchRecord createMatchRecord(String matchId, String puuid) {
    return createMatchRecord(matchId, puuid, 32.5, "Jinx", Role.ADC);
  }

  public static MatchRecord createMatchRecord(
      String matchId, String puuid, double gameDurationMinutes) {
    return createMatchRecord(matchId, puuid, gameDurationMinutes, "Jinx", Role.ADC);
  }

  public static MatchRecord createMatchRecord(
      String matchId, String puuid, double gameDurationMinutes, String championName, Role role) {
    return new MatchRecord(
        matchId,
        puuid,
        true,
        gameDurationMinutes,
        championName,
        role,
        // Combat
        8,
        3,
        12,
        4.5,
        2,
        850.0,
        1.2,
        0.28,
        0.18,
        0.65,
        // Economy
        420.0,
        7.8,
        // Objectives
        4500,
        12000,
        3,
        // Vision
        1.5,
        15,
        8,
        4,
        // Timeline
        78.0,
        3200.0,
        5800.0,
        6100.0);
  }

  public static MatchRecordEntity createMatchRecordEntity(String matchId, String puuid) {
    return createMatchRecordEntity(matchId, puuid, "Jinx", Role.ADC);
  }

  public static MatchRecordEntity createMatchRecordEntity(
      String matchId, String puuid, String championName, Role role) {
    return new MatchRecordEntity(
        matchId,
        puuid,
        true,
        32.5,
        // Combat
        championName,
        role.name(),
        8,
        3,
        12,
        4.5,
        2,
        850.0,
        1.2,
        0.28,
        0.18,
        0.65,
        // Economy
        420.0,
        7.8,
        // Objectives
        4500,
        12000,
        3,
        // Vision
        1.5,
        15,
        8,
        4,
        // Timeline
        78.0,
        3200.0,
        5800.0,
        6100.0);
  }
}
