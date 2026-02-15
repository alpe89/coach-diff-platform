package com.coachdiff.infrastructure.adapter.in.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.MatchDataNotFoundException;
import com.coachdiff.domain.model.MatchAggregate;
import com.coachdiff.domain.port.in.FetchMatchAggregatePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MatchAggregationController.class)
@TestPropertySource(
    properties = {
      "coach-diff.default-summoner.name = TestName",
      "coach-diff.default-summoner.tag = TestTag"
    })
class MatchAggregationControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private FetchMatchAggregatePort fetchMatchAggregatePort;

  @MockitoBean private MatchAggregationMapper matchAggregationMapper;

  @Test
  void shouldReturnMatchAggregation() throws Exception {
    var aggregate =
        new MatchAggregate(
            20, 12, 8, 6.5, 3.2, 8.1, 4.5, 1.8, 820.0, 1.9, 0.28, 0.20, 0.58, 410.0, 7.8, 78.0,
            3900.0, 5400.0, 5800.0, 2800.0, 6500.0, 2.3, 1.3, 11.0, 3.5, 4.2);

    when(fetchMatchAggregatePort.fetchMatchAggregation("TestName", "TestTag"))
        .thenReturn(aggregate);

    var dto =
        new com.coachdiff.infrastructure.adapter.in.rest.dto.MatchAggregationDto(
            20, 12, 8, 6.5, 3.2, 8.1, 4.5, 1.8, 820.0, 1.9, 0.28, 0.20, 0.58, 410.0, 7.8, 78.0,
            3900.0, 5400.0, 5800.0, 2800.0, 6500.0, 2.3, 1.3, 11.0, 3.5, 4.2, 0.6);

    when(matchAggregationMapper.toDto(aggregate)).thenReturn(dto);

    mockMvc
        .perform(get("/api/matches"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gamesAnalyzed").value(20))
        .andExpect(jsonPath("$.wins").value(12))
        .andExpect(jsonPath("$.losses").value(8))
        .andExpect(jsonPath("$.winRate").value(0.6))
        .andExpect(jsonPath("$.avgKills").value(6.5))
        .andExpect(jsonPath("$.avgDeaths").value(3.2))
        .andExpect(jsonPath("$.avgAssists").value(8.1))
        .andExpect(jsonPath("$.avgCsAt10").value(78.0))
        .andExpect(jsonPath("$.avgGoldAt15").value(5400.0));
  }

  @Test
  void shouldReturnNotFoundWhenNoMatchData() throws Exception {
    when(fetchMatchAggregatePort.fetchMatchAggregation("TestName", "TestTag"))
        .thenThrow(
            new MatchDataNotFoundException(
                ErrorCode.MATCH_DATA_NOT_FOUND, "No match data was found for TestName#TestTag"));

    mockMvc
        .perform(get("/api/matches"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.MATCH_DATA_NOT_FOUND.name()))
        .andExpect(jsonPath("$.message").value("No match data was found for TestName#TestTag"));
  }
}
