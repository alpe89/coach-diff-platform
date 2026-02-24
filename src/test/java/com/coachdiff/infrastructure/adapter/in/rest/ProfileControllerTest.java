package com.coachdiff.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Profile;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.in.FetchProfilePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private FetchProfilePort fetchProfilePort;

  @BeforeEach
  void setUp() {
    when(fetchProfilePort.getProfile("example@email.com"))
        .thenReturn(
            new Profile(
                "Summoner",
                "#1234",
                Region.EUW1,
                "https://ddragon.url",
                Tier.DIAMOND,
                Division.III,
                20,
                5,
                8,
                13,
                0.38));
  }

  @Test
  public void shouldReturnProfile() throws Exception {
    mockMvc
        .perform(get("/api/profile").header("X-User-Email", "example@email.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Summoner"))
        .andExpect(jsonPath("$.tag").value("#1234"))
        .andExpect(jsonPath("$.profileIconURI").value("https://ddragon.url"))
        .andExpect(jsonPath("$.tier").value("DIAMOND"))
        .andExpect(jsonPath("$.division").value("III"))
        .andExpect(jsonPath("$.lp").value(20))
        .andExpect(jsonPath("$.wins").value(5))
        .andExpect(jsonPath("$.losses").value(8))
        .andExpect(jsonPath("$.winRate").value(0.38))
        .andExpect(jsonPath("$.gamesPlayed").value(13));
  }

  @Test
  public void shouldReturnNotFound() throws Exception {
    when(fetchProfilePort.getProfile(any()))
        .thenThrow(
            new SummonerProfileNotFoundException(
                ErrorCode.SUMMONER_NOT_FOUND, "Profile not found"));

    mockMvc
        .perform(get("/api/profile").header("X-User-Email", "example@email.com"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.SUMMONER_NOT_FOUND.name()))
        .andExpect(jsonPath("$.message").value("Profile not found"));
  }
}
