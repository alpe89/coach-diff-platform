package com.coachdiff.application.service;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.MatchDataNotFoundException;
import com.coachdiff.domain.model.MatchAggregate;
import com.coachdiff.domain.port.in.FetchMatchAggregatePort;
import com.coachdiff.domain.port.out.LoadMatchAggregatePort;
import org.springframework.stereotype.Service;

@Service
public class FetchMatchAggregateService implements FetchMatchAggregatePort {
  private final LoadMatchAggregatePort loadMatchAggregatePort;

  FetchMatchAggregateService(LoadMatchAggregatePort loadMatchAggregatePort) {
    this.loadMatchAggregatePort = loadMatchAggregatePort;
  }

  @Override
  public MatchAggregate fetchMatchAggregation(String name, String tag) {
    return loadMatchAggregatePort
        .loadMatchAggregate(name, tag)
        .orElseThrow(
            () ->
                new MatchDataNotFoundException(
                    ErrorCode.MATCH_DATA_NOT_FOUND,
                    "No match data was found for " + name + "#" + tag));
  }
}
