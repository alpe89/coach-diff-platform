package com.coachdiff.domain.port.in;

import com.coachdiff.domain.model.MatchAggregate;

public interface FetchMatchAggregatePort {
  MatchAggregate fetchMatchAggregation(String email);
}
