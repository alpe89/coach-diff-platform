package com.coachdiff.application.service;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.Match;
import com.coachdiff.domain.model.MatchAggregate;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.port.in.FetchMatchAggregatePort;
import com.coachdiff.domain.port.out.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FetchMatchAggregateService implements FetchMatchAggregatePort {
  private static final Logger log = LoggerFactory.getLogger(FetchMatchAggregateService.class);
  private final FetchRiotAccountPort fetchRiotAccountPort;
  private final FetchMatchDetailsPort fetchMatchDetailsPort;
  private final LoadMatchRecordsPort loadMatchRecordsPort;
  private final SaveMatchRecordsPort saveMatchRecordsPort;
  private final Role coachingRole;

  FetchMatchAggregateService(
      FetchRiotAccountPort fetchRiotAccountPort,
      FetchMatchDetailsPort fetchMatchDetailsPort,
      LoadMatchRecordsPort loadMatchRecordsPort,
      SaveMatchRecordsPort saveMatchRecordsPort,
      @Value("${coach-diff.coaching-role}") String coachingRole) {
    this.fetchRiotAccountPort = fetchRiotAccountPort;
    this.fetchMatchDetailsPort = fetchMatchDetailsPort;
    this.loadMatchRecordsPort = loadMatchRecordsPort;
    this.saveMatchRecordsPort = saveMatchRecordsPort;
    this.coachingRole = Role.valueOf(coachingRole);
  }

  @Override
  public MatchAggregate fetchMatchAggregation(String name, String tag) {
    var puuid =
        fetchRiotAccountPort
            .getPuuid(name, tag)
            .orElseThrow(
                () ->
                    new SummonerProfileNotFoundException(
                        ErrorCode.SUMMONER_NOT_FOUND, name + "#" + tag));

    var matchIds = fetchMatchDetailsPort.getMatchIdsByPuuid(puuid);
    var matchRecords = loadMatchRecordsPort.loadExistingMatchRecords(puuid, matchIds);
    var matchIdsToFetch = excludeKnownMatchesIds(matchIds, matchRecords);
    log.info(
        "Match aggregation for {}#{}: {} total, {} from DB, {} to fetch from Riot",
        name,
        tag,
        matchIds.size(),
        matchRecords.size(),
        matchIdsToFetch.size());

    var fetchedMatches =
        fetchMatchDetailsPort.getMatchRecords(puuid, matchIdsToFetch).stream()
            .filter(match -> match.gameDurationMinutes() >= 10.0)
            .toList();

    if (!fetchedMatches.isEmpty()) {
      saveMatchRecordsPort.saveMatchRecords(fetchedMatches);
      log.info("Saved {} new match records to DB", fetchedMatches.size());
    }

    return MatchAggregate.fromMatchRecordList(
        Stream.concat(matchRecords.stream(), fetchedMatches.stream())
            .filter(match -> match.role() == coachingRole)
            .toList());
  }

  private List<String> excludeKnownMatchesIds(List<String> matchIds, List<Match> matches) {
    var existingIds = matches.stream().map(Match::matchId).collect(Collectors.toSet());

    return matchIds.stream().filter(matchId -> !existingIds.contains(matchId)).toList();
  }
}
