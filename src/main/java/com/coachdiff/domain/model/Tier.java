package com.coachdiff.domain.model;

import java.util.Optional;

public enum Tier {
  IRON,
  BRONZE,
  SILVER,
  GOLD,
  PLATINUM,
  EMERALD,
  DIAMOND,
  MASTER,
  GRANDMASTER,
  CHALLENGER;

  public Optional<Tier> nextTier() {
    return this == CHALLENGER ? Optional.empty() : Optional.of(Tier.values()[this.ordinal() + 1]);
  }

  public Optional<Tier> previousTier() {
    return this == IRON ? Optional.empty() : Optional.of(Tier.values()[this.ordinal() - 1]);
  }

  public boolean isApexTier() {
    return this == MASTER || this == GRANDMASTER || this == CHALLENGER;
  }
}
