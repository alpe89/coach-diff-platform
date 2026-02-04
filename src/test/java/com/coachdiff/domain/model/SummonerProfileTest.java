package com.coachdiff.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SummonerProfileTest {
  private SummonerProfile summoner;

  @BeforeEach
  void setUp() {
    this.summoner =
        new SummonerProfile("Random", "#1234", Region.EUW1, Tier.IRON, Division.III, 0, 12, 4);
  }

  @Test
  void shouldCalculateGamesPlayed() {
    assertThat(this.summoner.gamesPlayed()).isEqualTo(16);
  }

  @Test
  void shouldCalculateWinRate() {
    assertThat(this.summoner.winRate()).isCloseTo(0.75, Offset.offset(0.001));
  }

  @Test
  void shouldReturnZeroWinRateForZeroGamesPlayed() {
    summoner =
        new SummonerProfile("Random", "#1234", Region.EUW1, Tier.IRON, Division.III, 0, 0, 0);
    assertThat(this.summoner.winRate()).isEqualTo(0.0);
  }

  @ParameterizedTest
  @MethodSource("winRateFactory")
  void shouldReturnWinRate(int wins, int losses, double expectedWinRate) {
    summoner =
        new SummonerProfile(
            "Random", "#1234", Region.EUW1, Tier.IRON, Division.III, 0, wins, losses);
    assertThat(this.summoner.winRate()).isCloseTo(expectedWinRate, Offset.offset(0.001));
  }

  static Stream<Arguments> winRateFactory() {
    return Stream.of(Arguments.of(10, 0, 1.0), Arguments.of(0, 10, 0.0), Arguments.of(7, 3, 0.7));
  }
}
