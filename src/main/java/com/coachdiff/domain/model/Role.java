package com.coachdiff.domain.model;

public enum Role {
  ADC,
  JUNGLE,
  MID,
  TOP,
  SUPPORT,
  OTHER;

  public static Role fromRiotRole(String riotRole) {
    return switch (riotRole) {
      case "BOTTOM" -> ADC;
      case "MIDDLE" -> MID;
      case "TOP" -> TOP;
      case "JUNGLE" -> JUNGLE;
      case "UTILITY" -> SUPPORT;
      default -> OTHER;
    };
  }
}
