package com.coachdiff.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.exception.LeagueDataNotFoundException;
import com.coachdiff.domain.exception.SummonerDataNotFoundException;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.*;
import com.coachdiff.domain.port.out.FetchLeagueDataPort;
import com.coachdiff.domain.port.out.FetchRiotAccountPort;
import com.coachdiff.domain.port.out.FetchSummonerDataPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchProfileServiceTest {
  @Mock private FetchRiotAccountPort fetchRiotAccountPort;
  @Mock private FetchSummonerDataPort fetchSummonerDataPort;
  @Mock private FetchLeagueDataPort fetchLeagueDataPort;

  @Test
  void shouldGetProfile() {
    when(fetchRiotAccountPort.getPuuid(any(), any())).thenReturn(Optional.of("puuid"));
    when(fetchSummonerDataPort.getSummonerDataByPuuid("puuid"))
        .thenReturn(Optional.of(new Summoner("https://ddragon.mock.com/profile.png")));
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid"))
        .thenReturn(Optional.of(new Rank(Tier.EMERALD, Division.I, 11, 5, 2)));

    Profile profile =
        new FetchProfileService(fetchSummonerDataPort, fetchLeagueDataPort, fetchRiotAccountPort)
            .getProfile("aaa", "bbb");

    assertThat(profile.name()).isEqualTo("aaa");
    assertThat(profile.tier()).isEqualTo(Tier.EMERALD);
    assertThat(profile.profileIconURI()).isEqualTo("https://ddragon.mock.com/profile.png");
  }

  @Test
  void shouldThrowWhenSummonerNotFound() {
    when(fetchRiotAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                new FetchProfileService(
                        fetchSummonerDataPort, fetchLeagueDataPort, fetchRiotAccountPort)
                    .getProfile("fake-name", "fake-tag"))
        .isInstanceOf(SummonerProfileNotFoundException.class);
  }

  @Test
  void shouldThrowWhenLeagueDataNotFound() {
    when(fetchRiotAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.of("puuid"));
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid")).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                new FetchProfileService(
                        fetchSummonerDataPort, fetchLeagueDataPort, fetchRiotAccountPort)
                    .getProfile("fake-name", "fake-tag"))
        .isInstanceOf(LeagueDataNotFoundException.class);
  }

  @Test
  void shouldThrowWhenSummonerDataNotFound() {
    when(fetchRiotAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.of("puuid"));
    when(fetchSummonerDataPort.getSummonerDataByPuuid("puuid")).thenReturn(Optional.empty());
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid"))
        .thenReturn(Optional.of(new Rank(Tier.EMERALD, Division.I, 11, 5, 2)));

    assertThatThrownBy(
            () ->
                new FetchProfileService(
                        fetchSummonerDataPort, fetchLeagueDataPort, fetchRiotAccountPort)
                    .getProfile("fake-name", "fake-tag"))
        .isInstanceOf(SummonerDataNotFoundException.class);
  }
}
