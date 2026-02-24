package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.port.in.FetchMatchAggregatePort;
import com.coachdiff.infrastructure.adapter.in.rest.dto.MatchAggregationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MatchAggregationController {
  private final FetchMatchAggregatePort fetchMatchAggregatePort;
  private final MatchAggregationMapper matchAggregationMapper;

  public MatchAggregationController(
      FetchMatchAggregatePort fetchMatchAggregatePort,
      MatchAggregationMapper matchAggregationMapper) {
    this.fetchMatchAggregatePort = fetchMatchAggregatePort;
    this.matchAggregationMapper = matchAggregationMapper;
  }

  @GetMapping("/matches")
  public ResponseEntity<MatchAggregationDto> getMatchAggregation(
      @RequestHeader("X-User-Email") String email) {
    var aggregate = fetchMatchAggregatePort.fetchMatchAggregation(email);
    return ResponseEntity.ok(matchAggregationMapper.toDto(aggregate));
  }
}
