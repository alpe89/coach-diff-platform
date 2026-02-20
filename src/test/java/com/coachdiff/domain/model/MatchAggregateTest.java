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
            10, 7, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            List.of());
    assertThat(stats.winRate()).isCloseTo(0.7, within(0.001));
  }

  @Test
  void shouldReturnZeroWinRateWhenNoGamesAnalyzed() {
    MatchAggregate stats =
        new MatchAggregate(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            List.of());
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

  @Test
  void shouldGroupChampionBreakdownByChampionName() {
    var matches =
        List.of(
            createMatchRecord("match-1", "puuid", 30.0, "Jinx", Role.ADC),
            createMatchRecord("match-2", "puuid", 25.0, "Jinx", Role.ADC),
            createMatchRecord("match-3", "puuid", 28.0, "Kai'Sa", Role.ADC));

    MatchAggregate aggregate = MatchAggregate.fromMatchRecordList(matches);

    assertThat(aggregate.gamesAnalyzed()).isEqualTo(3);
    assertThat(aggregate.championsAggregate()).hasSize(2);

    var jinx =
        aggregate.championsAggregate().stream()
            .filter(c -> c.championName().equals("Jinx"))
            .findFirst()
            .orElseThrow();
    assertThat(jinx.gamesAnalyzed()).isEqualTo(2);
    assertThat(jinx.wins()).isEqualTo(2);
    assertThat(jinx.winRate()).isEqualTo(1.0);

    var kaisa =
        aggregate.championsAggregate().stream()
            .filter(c -> c.championName().equals("Kai'Sa"))
            .findFirst()
            .orElseThrow();
    assertThat(kaisa.gamesAnalyzed()).isEqualTo(1);
    assertThat(kaisa.wins()).isEqualTo(1);
  }
}
