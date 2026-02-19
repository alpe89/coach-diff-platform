package com.coachdiff.domain.model;

import static com.coachdiff.testutil.TestFixtures.createMatchRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
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

  @Test
  void shouldReturnMatchAggregateFromMatchRecord() {
    var matches =
        List.of(createMatchRecord("match-1", "puuid"), createMatchRecord("match-2", "puuid"));

    MatchAggregate aggregate = MatchAggregate.fromMatchRecordList(matches);
    assertThat(aggregate.gamesAnalyzed()).isEqualTo(2);
    assertThat(aggregate.wins()).isEqualTo(2);
    assertThat(aggregate.losses()).isEqualTo(0);
    assertThat(aggregate.winRate()).isEqualTo(1.0);
    assertThat(aggregate.avgKills()).isEqualTo(8.0);
  }
}
