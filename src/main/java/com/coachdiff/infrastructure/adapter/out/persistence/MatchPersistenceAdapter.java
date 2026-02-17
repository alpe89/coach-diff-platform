package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.MatchRecord;
import com.coachdiff.domain.port.out.LoadMatchRecordsPort;
import com.coachdiff.domain.port.out.SaveMatchRecordsPort;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MatchPersistenceAdapter implements SaveMatchRecordsPort, LoadMatchRecordsPort {

  private final MatchRecordRepository repository;

  public MatchPersistenceAdapter(MatchRecordRepository repository) {
    this.repository = repository;
  }

  @Override
  public void saveMatchRecords(List<MatchRecord> matchRecords) {
    var matchEntities = matchRecords.stream().map(MatchRecordEntity::from).toList();

    repository.saveAll(matchEntities);
  }

  @Override
  public List<MatchRecord> loadExistingMatchRecords(String puuid, List<String> matchIds) {
    var matchEntities = repository.findByPuuidAndMatchIdIn(puuid, matchIds);

    return matchEntities.stream().map(MatchRecordEntity::toDomain).toList();
  }
}
