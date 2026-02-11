package com.coachdiff.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class MatchAggregateTest {

  @Test
  void shouldCalculateWinRate() {
    MatchAggregate stats =
        new MatchAggregate(
            10, 7, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    assertThat(stats.winRate()).isCloseTo(0.7, within(0.001));
  }

  @Test
  void shouldReturnZeroWinRateWhenNoGamesAnalyzed() {
    MatchAggregate stats =
        new MatchAggregate(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    assertThat(stats.winRate()).isEqualTo(0.0);
  }
}
