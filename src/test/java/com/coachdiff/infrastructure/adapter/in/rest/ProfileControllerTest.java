package com.coachdiff.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.in.FetchProfilePort;
import java.math.BigDecimal;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileController.class)
@TestPropertySource(
    properties = {
      "coach-diff.default-summoner.name = TestName",
      "coach-diff.default-summoner.tag = TestTag"
    })
public class ProfileControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private FetchProfilePort fetchProfilePort;

  @BeforeEach
  void setUp() {
    when(fetchProfilePort.getSummonerProfile("TestName", "TestTag"))
        .thenReturn(
            new SummonerProfile(
                "Summoner", "#1234", Region.EUW1, Tier.DIAMOND, Division.III, 20, 5, 8));
  }

  @Test
  public void shouldReturnSummonerProfile() throws Exception {
    mockMvc
        .perform(get("/api/profile"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Summoner"))
        .andExpect(jsonPath("$.tag").value("#1234"))
        .andExpect(jsonPath("$.tier").value("DIAMOND"))
        .andExpect(jsonPath("$.division").value("III"))
        .andExpect(jsonPath("$.lp").value(20))
        .andExpect(jsonPath("$.wins").value(5))
        .andExpect(jsonPath("$.losses").value(8))
        .andExpect(
            jsonPath("$.winRate")
                .value(Matchers.closeTo(new BigDecimal("0.38"), new BigDecimal("0.01"))))
        .andExpect(jsonPath("$.gamesPlayed").value(13));
  }

  @Test
  public void shouldReturnNotFound() throws Exception {
    when(fetchProfilePort.getSummonerProfile(any(), any()))
        .thenThrow(
            new SummonerProfileNotFoundException(
                ErrorCode.SUMMONER_NOT_FOUND, "Profile not found"));

    mockMvc
        .perform(get("/api/profile"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.SUMMONER_NOT_FOUND.name()))
        .andExpect(jsonPath("$.message").value("Profile not found"));
  }
}
