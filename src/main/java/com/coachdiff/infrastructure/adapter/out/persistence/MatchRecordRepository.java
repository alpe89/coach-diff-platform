package com.coachdiff.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRecordRepository extends JpaRepository<MatchRecordEntity, MatchRecordId> {
  List<MatchRecordEntity> findByPuuidAndMatchIdIn(String puuid, List<String> matchIds);
}
