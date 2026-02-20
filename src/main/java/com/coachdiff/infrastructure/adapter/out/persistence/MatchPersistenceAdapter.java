package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.MatchRecord;
import com.coachdiff.domain.port.out.LoadMatchRecordsPort;
import com.coachdiff.domain.port.out.SaveMatchRecordsPort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MatchPersistenceAdapter implements SaveMatchRecordsPort, LoadMatchRecordsPort {
  private static final Logger log = LoggerFactory.getLogger(MatchPersistenceAdapter.class);
  private final MatchRecordRepository repository;

  public MatchPersistenceAdapter(MatchRecordRepository repository) {
    this.repository = repository;
  }

  @Override
  public void saveMatchRecords(List<MatchRecord> matchRecords) {
    var matchEntities = matchRecords.stream().map(MatchRecordEntity::from).toList();
    repository.saveAll(matchEntities);
    log.debug("Persisted {} match records", matchEntities.size());
  }

  @Override
  public List<MatchRecord> loadExistingMatchRecords(String puuid, List<String> matchIds) {
    var matchEntities = repository.findByPuuidAndMatchIdIn(puuid, matchIds);
    log.debug("Loaded {} existing match records from DB for puuid={}", matchEntities.size(), puuid);
    return matchEntities.stream().map(MatchRecordEntity::toDomain).toList();
  }
}
