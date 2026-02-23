package com.coachdiff.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<MatchEntity, MatchId> {
  List<MatchEntity> findByPuuidAndMatchIdIn(String puuid, List<String> matchIds);
}
