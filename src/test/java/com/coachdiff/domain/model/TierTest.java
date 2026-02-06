package com.coachdiff.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class TierTest {
  @ParameterizedTest
  @EnumSource(
      value = Tier.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"CHALLENGER"})
  public void shouldReturnNextTier(Tier currentTier) {
    assertThat(currentTier.nextTier()).isPresent();
  }

  @Test
  public void shouldReturnEmptyTierForChallenger() {
    assertThat(Tier.CHALLENGER.nextTier()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(
      value = Tier.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"IRON"})
  public void shouldReturnPreviousTier(Tier currentTier) {
    assertThat(currentTier.previousTier().isPresent());
  }

  @Test
  public void shouldReturnEmptyTierForIron() {
    assertThat(Tier.IRON.previousTier()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(
      value = Tier.class,
      mode = EnumSource.Mode.INCLUDE,
      names = {"MASTER", "GRANDMASTER", "CHALLENGER"})
  public void shouldReturnIsApexTier(Tier currentTier) {
    assertThat(currentTier.isApexTier()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(
      value = Tier.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"MASTER", "GRANDMASTER", "CHALLENGER"})
  public void shouldReturnIsNotApexTier(Tier currentTier) {
    assertThat(currentTier.isApexTier()).isFalse();
  }
}
