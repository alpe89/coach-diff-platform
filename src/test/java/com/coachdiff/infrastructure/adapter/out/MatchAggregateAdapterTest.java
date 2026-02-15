package com.coachdiff.infrastructure.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import com.coachdiff.infrastructure.adapter.out.dto.RiotMatchDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotTimelineDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchAggregateAdapterTest {
  @Mock private RiotAccountClient riotAccountClient;
  @Mock private RiotMatchClient riotMatchClient;

  private MatchAggregateAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new MatchAggregateAdapter(riotAccountClient, riotMatchClient);
  }

  @Test
  void shouldReturnEmptyWhenAccountNotFound() {
    when(riotAccountClient.getRiotAccountPuuid("unknown", "user")).thenReturn(Optional.empty());

    var result = adapter.loadMatchAggregate("unknown", "user");

    assertThat(result).isEmpty();
  }

  @Test
  void shouldAggregateMatchData() {
    when(riotAccountClient.getRiotAccountPuuid("test", "1234")).thenReturn(Optional.of("my-puuid"));

    when(riotMatchClient.getMatchesIds("my-puuid")).thenReturn(List.of("EUW1_1111", "EUW1_2222"));

    when(riotMatchClient.getMatchData("EUW1_1111")).thenReturn(buildMatch(true, 10, 2, 5, 1800));
    when(riotMatchClient.getMatchData("EUW1_2222")).thenReturn(buildMatch(false, 4, 6, 8, 2400));

    when(riotMatchClient.getMatchTimelineData("EUW1_1111"))
        .thenReturn(buildTimeline("my-puuid", 80, 10, 4000, 5500, 6000));
    when(riotMatchClient.getMatchTimelineData("EUW1_2222"))
        .thenReturn(buildTimeline("my-puuid", 60, 5, 3500, 5000, 5500));

    var result = adapter.loadMatchAggregate("test", "1234");

    assertThat(result).isPresent();

    var aggregate = result.get();
    assertThat(aggregate.gamesAnalyzed()).isEqualTo(2);
    assertThat(aggregate.wins()).isEqualTo(1);
    assertThat(aggregate.losses()).isEqualTo(1);

    // avg kills = (10 + 4) / 2 = 7.0
    assertThat(aggregate.avgKills()).isCloseTo(7.0, within(0.01));
    // avg deaths = (2 + 6) / 2 = 4.0
    assertThat(aggregate.avgDeaths()).isCloseTo(4.0, within(0.01));
    // avg assists = (5 + 8) / 2 = 6.5
    assertThat(aggregate.avgAssists()).isCloseTo(6.5, within(0.01));

    // Timeline: avg CS@10 = (80+10 + 60+5) / 2 = 77.5
    assertThat(aggregate.avgCsAt10()).isCloseTo(77.5, within(0.01));
    // avg gold@10 = (4000 + 3500) / 2 = 3750
    assertThat(aggregate.avgGoldAt10()).isCloseTo(3750.0, within(0.01));
    // avg gold@15 = (5500 + 5000) / 2 = 5250
    assertThat(aggregate.avgGoldAt15()).isCloseTo(5250.0, within(0.01));
    // avg xp@15 = (6000 + 5500) / 2 = 5750
    assertThat(aggregate.avgXpAt15()).isCloseTo(5750.0, within(0.01));
  }

  @Test
  void shouldHandleShortGamesWithoutTimelineData() {
    when(riotAccountClient.getRiotAccountPuuid("test", "1234")).thenReturn(Optional.of("my-puuid"));

    when(riotMatchClient.getMatchesIds("my-puuid")).thenReturn(List.of("EUW1_1111"));

    when(riotMatchClient.getMatchData("EUW1_1111")).thenReturn(buildMatch(true, 5, 1, 3, 480));

    // Short game: only 8 frames (< 10 min)
    when(riotMatchClient.getMatchTimelineData("EUW1_1111"))
        .thenReturn(buildShortTimeline("my-puuid", 8));

    var result = adapter.loadMatchAggregate("test", "1234");

    assertThat(result).isPresent();
    var aggregate = result.get();
    assertThat(aggregate.gamesAnalyzed()).isEqualTo(1);
    assertThat(aggregate.avgKills()).isCloseTo(5.0, within(0.01));

    // No timeline data — should be 0
    assertThat(aggregate.avgCsAt10()).isEqualTo(0.0);
    assertThat(aggregate.avgGoldAt10()).isEqualTo(0.0);
    assertThat(aggregate.avgGoldAt15()).isEqualTo(0.0);
  }

  @Test
  void shouldHandleGameBetween10And15Minutes() {
    when(riotAccountClient.getRiotAccountPuuid("test", "1234")).thenReturn(Optional.of("my-puuid"));

    when(riotMatchClient.getMatchesIds("my-puuid")).thenReturn(List.of("EUW1_1111"));

    when(riotMatchClient.getMatchData("EUW1_1111")).thenReturn(buildMatch(false, 2, 5, 1, 720));

    // 12 frames: has @10 but not @15
    when(riotMatchClient.getMatchTimelineData("EUW1_1111"))
        .thenReturn(buildTimelineWithoutAt15("my-puuid", 70, 8, 3800));

    var result = adapter.loadMatchAggregate("test", "1234");

    assertThat(result).isPresent();
    var aggregate = result.get();

    // @10 data should exist
    assertThat(aggregate.avgCsAt10()).isCloseTo(78.0, within(0.01));
    assertThat(aggregate.avgGoldAt10()).isCloseTo(3800.0, within(0.01));

    // @15 data should be 0 (game too short)
    assertThat(aggregate.avgGoldAt15()).isEqualTo(0.0);
    assertThat(aggregate.avgXpAt15()).isEqualTo(0.0);
  }

  @Test
  void shouldReturnZeroAveragesWhenNoParticipantFound() {
    when(riotAccountClient.getRiotAccountPuuid("test", "1234")).thenReturn(Optional.of("my-puuid"));

    when(riotMatchClient.getMatchesIds("my-puuid")).thenReturn(List.of("EUW1_1111"));

    // Match where "my-puuid" is NOT in the participants
    when(riotMatchClient.getMatchData("EUW1_1111")).thenReturn(buildMatchWithDifferentPuuid());

    when(riotMatchClient.getMatchTimelineData("EUW1_1111"))
        .thenReturn(buildTimeline("other-puuid", 80, 10, 4000, 5500, 6000));

    var result = adapter.loadMatchAggregate("test", "1234");

    assertThat(result).isPresent();
    var aggregate = result.get();
    assertThat(aggregate.gamesAnalyzed()).isEqualTo(0);
    assertThat(aggregate.avgKills()).isEqualTo(0.0);
  }

  // --- Test data builders ---

  private RiotMatchDTO buildMatch(boolean win, int kills, int deaths, int assists, int duration) {
    var challenges =
        new RiotMatchDTO.Challenges(
            deaths == 0 ? (kills + assists) : (double) (kills + assists) / deaths,
            2, // soloKills
            (double) 20000 / (duration / 60.0), // damagePerMinute
            0.25, // teamDamagePercentage
            0.20, // damageTakenOnTeamPercentage
            0.55, // killParticipation
            (double) 10000 / (duration / 60.0), // goldPerMinute
            1.2, // visionScorePerMinute
            4, // controlWardsPlaced
            2); // turretPlatesTaken

    var participant =
        new RiotMatchDTO.Participant(
            "my-puuid",
            win,
            kills,
            deaths,
            assists,
            20000, // totalDamageDealtToChampions
            10000, // goldEarned
            2500, // damageDealtToTurrets
            7000, // damageDealtToObjectives
            150, // totalMinionsKilled
            15, // neutralMinionsKilled
            10, // wardsPlaced
            3, // wardsKilled
            challenges);

    return new RiotMatchDTO(new RiotMatchDTO.Info(duration, List.of(participant)));
  }

  private RiotMatchDTO buildMatchWithDifferentPuuid() {
    var challenges = new RiotMatchDTO.Challenges(3.0, 1, 500.0, 0.20, 0.18, 0.40, 350.0, 1.0, 2, 1);
    var participant =
        new RiotMatchDTO.Participant(
            "other-puuid", true, 5, 2, 3, 15000, 8000, 1000, 3000, 100, 10, 8, 2, challenges);
    return new RiotMatchDTO(new RiotMatchDTO.Info(1800, List.of(participant)));
  }

  private RiotTimelineDTO buildTimeline(
      String puuid, int minionsAt10, int jungleAt10, int goldAt10, int goldAt15, int xpAt15) {
    var metadata = new RiotTimelineDTO.Metadata(List.of(puuid));

    // Build 16 frames (0 through 15)
    var emptyFrame =
        new RiotTimelineDTO.Frame(
            0, Map.of("1", new RiotTimelineDTO.ParticipantFrame(0, 0, 500, 0)));

    var frameAt10 =
        new RiotTimelineDTO.Frame(
            600000,
            Map.of(
                "1",
                new RiotTimelineDTO.ParticipantFrame(minionsAt10, jungleAt10, goldAt10, 4000)));

    var frameAt15 =
        new RiotTimelineDTO.Frame(
            900000,
            Map.of(
                "1",
                new RiotTimelineDTO.ParticipantFrame(
                    minionsAt10 + 40, jungleAt10 + 5, goldAt15, xpAt15)));

    // Frames 0-9 are empty, frame 10 has data, frames 11-14 are empty, frame 15 has data
    var frames = new java.util.ArrayList<RiotTimelineDTO.Frame>();
    for (int i = 0; i < 10; i++) {
      frames.add(emptyFrame);
    }
    frames.add(frameAt10);
    for (int i = 11; i < 15; i++) {
      frames.add(emptyFrame);
    }
    frames.add(frameAt15);

    return new RiotTimelineDTO(metadata, new RiotTimelineDTO.Info(60000, frames));
  }

  private RiotTimelineDTO buildShortTimeline(String puuid, int frameCount) {
    var metadata = new RiotTimelineDTO.Metadata(List.of(puuid));
    var emptyFrame =
        new RiotTimelineDTO.Frame(
            0, Map.of("1", new RiotTimelineDTO.ParticipantFrame(0, 0, 500, 0)));

    var frames = new java.util.ArrayList<RiotTimelineDTO.Frame>();
    for (int i = 0; i < frameCount; i++) {
      frames.add(emptyFrame);
    }
    return new RiotTimelineDTO(metadata, new RiotTimelineDTO.Info(60000, frames));
  }

  private RiotTimelineDTO buildTimelineWithoutAt15(
      String puuid, int minionsAt10, int jungleAt10, int goldAt10) {
    var metadata = new RiotTimelineDTO.Metadata(List.of(puuid));
    var emptyFrame =
        new RiotTimelineDTO.Frame(
            0, Map.of("1", new RiotTimelineDTO.ParticipantFrame(0, 0, 500, 0)));

    var frameAt10 =
        new RiotTimelineDTO.Frame(
            600000,
            Map.of(
                "1",
                new RiotTimelineDTO.ParticipantFrame(minionsAt10, jungleAt10, goldAt10, 4000)));

    // 12 frames: 0-9 empty, 10 has data, 11 empty (no frame 15)
    var frames = new java.util.ArrayList<RiotTimelineDTO.Frame>();
    for (int i = 0; i < 10; i++) {
      frames.add(emptyFrame);
    }
    frames.add(frameAt10);
    frames.add(emptyFrame);

    return new RiotTimelineDTO(metadata, new RiotTimelineDTO.Info(60000, frames));
  }
}
