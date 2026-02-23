package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.Match;
import com.coachdiff.domain.port.out.LoadMatchRecordsPort;
import com.coachdiff.domain.port.out.SaveMatchRecordsPort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MatchPersistenceAdapter implements SaveMatchRecordsPort, LoadMatchRecordsPort {
  private static final Logger log = LoggerFactory.getLogger(MatchPersistenceAdapter.class);
  private final MatchRepository repository;

  public MatchPersistenceAdapter(MatchRepository repository) {
    this.repository = repository;
  }

  @Override
  public void saveMatchRecords(List<Match> matches) {
    var matchEntities = matches.stream().map(MatchEntity::from).toList();
    repository.saveAll(matchEntities);
    log.debug("Persisted {} match records", matchEntities.size());
  }

  @Override
  public List<Match> loadExistingMatchRecords(String puuid, List<String> matchIds) {
    var matchEntities = repository.findByPuuidAndMatchIdIn(puuid, matchIds);
    log.debug("Loaded {} existing match records from DB for puuid={}", matchEntities.size(), puuid);
    return matchEntities.stream().map(MatchEntity::toDomain).toList();
  }
}
