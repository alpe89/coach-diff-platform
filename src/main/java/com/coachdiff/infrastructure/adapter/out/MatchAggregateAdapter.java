package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.domain.model.MatchAggregate;
import com.coachdiff.domain.port.out.LoadMatchAggregatePort;
import com.coachdiff.infrastructure.adapter.out.dto.RiotMatchDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotTimelineDTO;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MatchAggregateAdapter implements LoadMatchAggregatePort {
  private static final Logger log = LoggerFactory.getLogger(MatchAggregateAdapter.class);
  private final RiotAccountClient riotAccountClient;
  private final RiotMatchClient riotMatchClient;

  MatchAggregateAdapter(RiotAccountClient riotAccountClient, RiotMatchClient riotMatchClient) {
    this.riotAccountClient = riotAccountClient;
    this.riotMatchClient = riotMatchClient;
  }

  @Override
  public Optional<MatchAggregate> loadMatchAggregate(String name, String tag) {
    return riotAccountClient
        .getRiotAccountPuuid(name, tag)
        .flatMap(
            puuid -> {
              var matchesIds = riotMatchClient.getMatchesIds(puuid);

              try (var scope =
                  StructuredTaskScope.open(
                      StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
                StructuredTaskScope.Subtask<List<RiotMatchDTO>> matchDetailsTask =
                    scope.fork(() -> getMatchesDetails(matchesIds));
                StructuredTaskScope.Subtask<List<RiotTimelineDTO>> matchTimelineTask =
                    scope.fork(() -> getMatchesTimelines(matchesIds));

                scope.join();

                var matchDetails = matchDetailsTask.get();
                var matchTimeline = matchTimelineTask.get();

                return Optional.of(combineMatchDataToAggregate(puuid, matchDetails, matchTimeline));
              } catch (InterruptedException e) {
                log.error("Thread interrupted while fetching match data", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
              } catch (StructuredTaskScope.FailedException e) {

                if (e.getCause() instanceof RuntimeException re) {
                  throw re;
                }

                throw new RuntimeException(e.getCause());
              }
            });
  }

  private List<RiotMatchDTO> getMatchesDetails(List<String> matchIds) {
    return matchIds.stream().map(riotMatchClient::getMatchData).toList();
  }

  private List<RiotTimelineDTO> getMatchesTimelines(List<String> matchIds) {
    return matchIds.stream().map(riotMatchClient::getMatchTimelineData).toList();
  }

  private Optional<RiotMatchDTO.Participant> findMatchParticipantByPuuid(
      String puuid, RiotMatchDTO matchDetails) {
    return matchDetails.info().participants().stream()
        .filter(participant -> participant.puuid().equals(puuid))
        .findFirst();
  }

  private Optional<TimelineFrames> findTimelineFrames(String puuid, RiotTimelineDTO timeline) {
    int index = timeline.metadata().participants().indexOf(puuid);

    if (index == -1) {
      return Optional.empty();
    }

    String participantId = String.valueOf(index + 1);
    List<RiotTimelineDTO.Frame> frames = timeline.info().frames();

    if (frames.size() <= 10) {
      return Optional.empty();
    }

    var at10 = frames.get(10).participantFrames().get(participantId);
    var at15 =
        Optional.ofNullable(
            frames.size() <= 15 ? null : frames.get(15).participantFrames().get(participantId));

    if (at10 == null) {
      return Optional.empty();
    }

    return Optional.of(new TimelineFrames(at10, at15));
  }

  private MatchAggregate combineMatchDataToAggregate(
      String puuid, List<RiotMatchDTO> matchesDetails, List<RiotTimelineDTO> matchesTimeline) {
    int gamesAnalyzed = 0;
    int wins = 0;

    // Combat
    double totalKills = 0.0;
    double totalDeaths = 0.0;
    double totalAssists = 0.0;
    double totalKda = 0.0;
    double totalSoloKills = 0.0;
    double totalDamagePerMinute = 0.0;
    double totalDamagePerGold = 0.0;
    double totalTeamDamagePercentage = 0.0;
    double totalDamageTakenPercentage = 0.0;
    double totalKillParticipation = 0.0;

    // Economy
    double totalGoldPerMinute = 0.0;
    double totalCsPerMinute = 0.0;

    // Objectives
    double totalDamageToTurrets = 0.0;
    double totalDamageToObjectives = 0.0;
    double totalTurretPlatesTaken = 0.0;

    // Vision
    double totalVisionScorePerMinute = 0.0;
    double totalWardsPlaced = 0.0;
    double totalWardsKilled = 0.0;
    double totalControlWardsPlaced = 0.0;

    // Timeline — separate counters since not every game has these
    int gamesWithAt10 = 0;
    int gamesWithAt15 = 0;
    double totalCsAt10 = 0.0;
    double totalGoldAt10 = 0.0;
    double totalGoldAt15 = 0.0;
    double totalXpAt15 = 0.0;

    for (int i = 0; i < matchesDetails.size(); i++) {
      var participant = findMatchParticipantByPuuid(puuid, matchesDetails.get(i));
      if (participant.isEmpty()) {
        continue;
      }

      var p = participant.get();
      var c = p.challenges();
      double gameDurationMinutes = matchesDetails.get(i).info().gameDuration() / 60.0;

      gamesAnalyzed++;
      wins += p.win() ? 1 : 0;

      // Combat
      totalKills += p.kills();
      totalDeaths += p.deaths();
      totalAssists += p.assists();
      totalKda += c.kdaOrZero();
      totalSoloKills += c.soloKillsOrZero();
      totalDamagePerMinute += c.damagePerMinuteOrZero();
      totalDamagePerGold +=
          p.goldEarned() == 0 ? 0.0 : (double) p.totalDamageDealtToChampions() / p.goldEarned();
      totalTeamDamagePercentage += c.teamDamagePercentageOrZero();
      totalDamageTakenPercentage += c.damageTakenOnTeamPercentageOrZero();
      totalKillParticipation += c.killParticipationOrZero();

      // Economy
      totalGoldPerMinute += c.goldPerMinuteOrZero();
      totalCsPerMinute += (p.totalMinionsKilled() + p.neutralMinionsKilled()) / gameDurationMinutes;

      // Objectives
      totalDamageToTurrets += p.damageDealtToTurrets();
      totalDamageToObjectives += p.damageDealtToObjectives();
      totalTurretPlatesTaken += c.turretPlatesTakenOrZero();

      // Vision
      totalVisionScorePerMinute += c.visionScorePerMinuteOrZero();
      totalWardsPlaced += p.wardsPlaced();
      totalWardsKilled += p.wardsKilled();
      totalControlWardsPlaced += c.controlWardsPlacedOrZero();

      // Timeline
      var timelineFrames = findTimelineFrames(puuid, matchesTimeline.get(i));
      if (timelineFrames.isPresent()) {
        var tf = timelineFrames.get();
        gamesWithAt10++;
        totalCsAt10 += tf.at10().minionsKilled() + tf.at10().jungleMinionsKilled();
        totalGoldAt10 += tf.at10().totalGold();

        if (tf.at15().isPresent()) {
          var at15 = tf.at15().get();
          gamesWithAt15++;
          totalGoldAt15 += at15.totalGold();
          totalXpAt15 += at15.xp();
        }
      }
    }

    double n = gamesAnalyzed;
    double n10 = gamesWithAt10;
    double n15 = gamesWithAt15;

    return new MatchAggregate(
        gamesAnalyzed,
        wins,
        gamesAnalyzed - wins,
        // Combat
        n == 0 ? 0.0 : totalKills / n,
        n == 0 ? 0.0 : totalDeaths / n,
        n == 0 ? 0.0 : totalAssists / n,
        n == 0 ? 0.0 : totalKda / n,
        n == 0 ? 0.0 : totalSoloKills / n,
        n == 0 ? 0.0 : totalDamagePerMinute / n,
        n == 0 ? 0.0 : totalDamagePerGold / n,
        n == 0 ? 0.0 : totalTeamDamagePercentage / n,
        n == 0 ? 0.0 : totalDamageTakenPercentage / n,
        n == 0 ? 0.0 : totalKillParticipation / n,
        // Economy
        n == 0 ? 0.0 : totalGoldPerMinute / n,
        n == 0 ? 0.0 : totalCsPerMinute / n,
        n10 == 0 ? 0.0 : totalCsAt10 / n10,
        n10 == 0 ? 0.0 : totalGoldAt10 / n10,
        n15 == 0 ? 0.0 : totalGoldAt15 / n15,
        n15 == 0 ? 0.0 : totalXpAt15 / n15,
        // Objectives
        n == 0 ? 0.0 : totalDamageToTurrets / n,
        n == 0 ? 0.0 : totalDamageToObjectives / n,
        n == 0 ? 0.0 : totalTurretPlatesTaken / n,
        // Vision
        n == 0 ? 0.0 : totalVisionScorePerMinute / n,
        n == 0 ? 0.0 : totalWardsPlaced / n,
        n == 0 ? 0.0 : totalWardsKilled / n,
        n == 0 ? 0.0 : totalControlWardsPlaced / n);
  }

  private record TimelineFrames(
      RiotTimelineDTO.ParticipantFrame at10, Optional<RiotTimelineDTO.ParticipantFrame> at15) {}
}
