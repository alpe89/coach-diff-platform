package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.MatchAggregate;
import java.util.Optional;

public interface LoadMatchAggregatePort {
  Optional<MatchAggregate> loadMatchAggregate(String name, String tag);
}
