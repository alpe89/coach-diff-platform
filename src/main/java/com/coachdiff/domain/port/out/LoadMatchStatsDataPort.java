package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.MatchAggregate;
import java.util.Optional;

public interface LoadMatchStatsDataPort {
  Optional<MatchAggregate> loadMatchStatsData(String name, String tag);
}
