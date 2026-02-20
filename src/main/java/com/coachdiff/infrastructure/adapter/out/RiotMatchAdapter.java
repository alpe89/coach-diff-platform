package com.coachdiff.infrastructure.adapter.out;

import com.coachdiff.domain.model.MatchRecord;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.port.out.FetchMatchDetailsPort;
import com.coachdiff.infrastructure.adapter.out.dto.RiotMatchDTO;
import com.coachdiff.infrastructure.adapter.out.dto.RiotTimelineDTO;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RiotMatchAdapter implements FetchMatchDetailsPort {
  private static final Logger log = LoggerFactory.getLogger(RiotMatchAdapter.class);
  private final RiotMatchClient riotMatchClient;

  RiotMatchAdapter(RiotMatchClient riotMatchClient) {
    this.riotMatchClient = riotMatchClient;
  }

  @Override
  public List<String> getMatchIdsByPuuid(String puuid) {
    return riotMatchClient.getMatchesIds(puuid);
  }

  @Override
  public List<MatchRecord> getMatchRecords(String puuid, List<String> matchIds) {
    try (var scope =
        StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())) {
      StructuredTaskScope.Subtask<List<RiotMatchDTO>> matchDetailsTask =
          scope.fork(() -> getMatchesDetails(matchIds));
      StructuredTaskScope.Subtask<List<RiotTimelineDTO>> matchTimelineTask =
          scope.fork(() -> getMatchesTimelines(matchIds));

      scope.join();

      var matchDetails = matchDetailsTask.get();
      var matchTimelines = matchTimelineTask.get();

      return combineToMatchRecords(puuid, matchIds, matchDetails, matchTimelines);
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
  }

  private List<RiotMatchDTO> getMatchesDetails(List<String> matchIds) {
    return matchIds.stream().map(riotMatchClient::getMatchData).toList();
  }

  private List<RiotTimelineDTO> getMatchesTimelines(List<String> matchIds) {
    return matchIds.stream().map(riotMatchClient::getMatchTimelineData).toList();
  }

  private List<MatchRecord> combineToMatchRecords(
      String puuid,
      List<String> matchIds,
      List<RiotMatchDTO> matchDetails,
      List<RiotTimelineDTO> matchTimelines) {
    return IntStream.range(0, matchDetails.size())
        .mapToObj(
            i ->
                findParticipantByPuuid(puuid, matchDetails.get(i))
                    .map(
                        p ->
                            toMatchRecord(
                                matchIds.get(i),
                                matchDetails.get(i),
                                matchTimelines.get(i),
                                p,
                                puuid)))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<RiotMatchDTO.Participant> findParticipantByPuuid(
      String puuid, RiotMatchDTO match) {
    return match.info().participants().stream().filter(p -> p.puuid().equals(puuid)).findFirst();
  }

  private MatchRecord toMatchRecord(
      String matchId,
      RiotMatchDTO match,
      RiotTimelineDTO timeline,
      RiotMatchDTO.Participant p,
      String puuid) {
    var c = p.challenges();
    double gameDurationMinutes = match.info().gameDuration() / 60.0;
    double csPerMinute = (p.totalMinionsKilled() + p.neutralMinionsKilled()) / gameDurationMinutes;
    double damagePerGold =
        p.goldEarned() == 0 ? 0.0 : (double) p.totalDamageDealtToChampions() / p.goldEarned();

    var timelineFrames = extractTimelineFrames(puuid, timeline);
    Double csAt10 = null;
    Double goldAt10 = null;
    Double goldAt15 = null;
    Double xpAt15 = null;

    if (timelineFrames.isPresent()) {
      var at10 = timelineFrames.get().at10();
      csAt10 = (double) (at10.minionsKilled() + at10.jungleMinionsKilled());
      goldAt10 = (double) at10.totalGold();

      if (timelineFrames.get().at15().isPresent()) {
        var at15 = timelineFrames.get().at15().get();
        goldAt15 = (double) at15.totalGold();
        xpAt15 = (double) at15.xp();
      }
    }

    return new MatchRecord(
        matchId,
        puuid,
        p.win(),
        gameDurationMinutes,
        p.championName(),
        Role.fromRiotRole(p.teamPosition()),
        p.kills(),
        p.deaths(),
        p.assists(),
        c.kdaOrZero(),
        c.soloKillsOrZero(),
        c.damagePerMinuteOrZero(),
        damagePerGold,
        c.teamDamagePercentageOrZero(),
        c.damageTakenOnTeamPercentageOrZero(),
        c.killParticipationOrZero(),
        c.goldPerMinuteOrZero(),
        csPerMinute,
        p.damageDealtToTurrets(),
        p.damageDealtToObjectives(),
        c.turretPlatesTakenOrZero(),
        c.visionScorePerMinuteOrZero(),
        p.wardsPlaced(),
        p.wardsKilled(),
        c.controlWardsPlacedOrZero(),
        csAt10,
        goldAt10,
        goldAt15,
        xpAt15);
  }

  private Optional<TimelineFrames> extractTimelineFrames(String puuid, RiotTimelineDTO timeline) {
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

  private record TimelineFrames(
      RiotTimelineDTO.ParticipantFrame at10, Optional<RiotTimelineDTO.ParticipantFrame> at15) {}
}
