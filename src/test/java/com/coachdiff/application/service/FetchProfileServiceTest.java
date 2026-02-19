package com.coachdiff.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.exception.LeagueDataNotFoundException;
import com.coachdiff.domain.exception.SummonerDataNotFoundException;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.*;
import com.coachdiff.domain.port.out.FetchAccountPort;
import com.coachdiff.domain.port.out.FetchLeagueDataPort;
import com.coachdiff.domain.port.out.FetchSummonerDataPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchProfileServiceTest {
  @Mock private FetchAccountPort fetchAccountPort;
  @Mock private FetchSummonerDataPort fetchSummonerDataPort;
  @Mock private FetchLeagueDataPort fetchLeagueDataPort;

  @Test
  void shouldGetProfile() {
    when(fetchAccountPort.getPuuid(any(), any())).thenReturn(Optional.of("puuid"));
    when(fetchSummonerDataPort.getSummonerDataByPuuid("puuid"))
        .thenReturn(Optional.of(new SummonerRecord("https://ddragon.mock.com/profile.png")));
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid"))
        .thenReturn(Optional.of(new RankRecord(Tier.EMERALD, Division.I, 11, 5, 2)));

    Profile profile =
        new FetchProfileService(fetchSummonerDataPort, fetchLeagueDataPort, fetchAccountPort)
            .getProfile("aaa", "bbb");

    assertThat(profile.name()).isEqualTo("aaa");
    assertThat(profile.tier()).isEqualTo(Tier.EMERALD);
    assertThat(profile.profileIconURI()).isEqualTo("https://ddragon.mock.com/profile.png");
  }

  @Test
  void shouldThrowWhenSummonerNotFound() throws SummonerProfileNotFoundException {
    when(fetchAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.empty());
    assertThrows(
        SummonerProfileNotFoundException.class,
        () ->
            new FetchProfileService(fetchSummonerDataPort, fetchLeagueDataPort, fetchAccountPort)
                .getProfile("fake-name", "fake-tag"));
  }

  @Test
  void shouldThrowWhenLeagueDataNotFound() throws LeagueDataNotFoundException {
    when(fetchAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.of("puuid"));
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid")).thenReturn(Optional.empty());
    assertThrows(
        LeagueDataNotFoundException.class,
        () ->
            new FetchProfileService(fetchSummonerDataPort, fetchLeagueDataPort, fetchAccountPort)
                .getProfile("fake-name", "fake-tag"));
  }

  @Test
  void shouldThrowWhenSummonerDataNotFound() throws SummonerDataNotFoundException {
    when(fetchAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.of("puuid"));
    when(fetchSummonerDataPort.getSummonerDataByPuuid("puuid")).thenReturn(Optional.empty());
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid"))
        .thenReturn(Optional.of(new RankRecord(Tier.EMERALD, Division.I, 11, 5, 2)));

    assertThrows(
        SummonerDataNotFoundException.class,
        () ->
            new FetchProfileService(fetchSummonerDataPort, fetchLeagueDataPort, fetchAccountPort)
                .getProfile("fake-name", "fake-tag"));
  }
}
