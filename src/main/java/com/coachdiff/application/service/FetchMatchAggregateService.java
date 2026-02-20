package com.coachdiff.application.service;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.MatchAggregate;
import com.coachdiff.domain.model.MatchRecord;
import com.coachdiff.domain.port.in.FetchMatchAggregatePort;
import com.coachdiff.domain.port.out.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FetchMatchAggregateService implements FetchMatchAggregatePort {
  private static final Logger log = LoggerFactory.getLogger(FetchMatchAggregateService.class);
  private final FetchAccountPort fetchAccountPort;
  private final FetchMatchDetailsPort fetchMatchDetailsPort;
  private final LoadMatchRecordsPort loadMatchRecordsPort;
  private final SaveMatchRecordsPort saveMatchRecordsPort;

  FetchMatchAggregateService(
      FetchAccountPort fetchAccountPort,
      FetchMatchDetailsPort fetchMatchDetailsPort,
      LoadMatchRecordsPort loadMatchRecordsPort,
      SaveMatchRecordsPort saveMatchRecordsPort) {
    this.fetchAccountPort = fetchAccountPort;
    this.fetchMatchDetailsPort = fetchMatchDetailsPort;
    this.loadMatchRecordsPort = loadMatchRecordsPort;
    this.saveMatchRecordsPort = saveMatchRecordsPort;
  }

  @Override
  public MatchAggregate fetchMatchAggregation(String name, String tag) {
    var puuid =
        fetchAccountPort
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

    var fetchedMatches = fetchMatchDetailsPort.getMatchRecords(puuid, matchIdsToFetch);
    if (!fetchedMatches.isEmpty()) {
      saveMatchRecordsPort.saveMatchRecords(fetchedMatches);
      log.info("Saved {} new match records to DB", fetchedMatches.size());
    }

    return MatchAggregate.fromMatchRecordList(
        Stream.concat(matchRecords.stream(), fetchedMatches.stream()).toList());
  }

  private List<String> excludeKnownMatchesIds(
      List<String> matchIds, List<MatchRecord> matchRecords) {
    var existingIds = matchRecords.stream().map(MatchRecord::matchId).collect(Collectors.toSet());

    return matchIds.stream().filter(matchId -> !existingIds.contains(matchId)).toList();
  }
}
