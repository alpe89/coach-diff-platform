package com.coachdiff.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.exception.LeagueDataNotFoundException;
import com.coachdiff.domain.exception.SummonerDataNotFoundException;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.*;
import com.coachdiff.domain.port.out.AccountPersistencePort;
import com.coachdiff.domain.port.out.FetchLeagueDataPort;
import com.coachdiff.domain.port.out.FetchRiotAccountPort;
import com.coachdiff.domain.port.out.FetchSummonerDataPort;
import java.util.Map;
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
  @Mock private AccountPersistencePort accountPersistencePort;

  @Test
  void shouldGetProfile() {
    when(fetchRiotAccountPort.getPuuid(any(), any())).thenReturn(Optional.of("puuid"));
    when(fetchSummonerDataPort.getSummonerDataByPuuid("puuid"))
        .thenReturn(Optional.of(new Summoner("https://ddragon.mock.com/profile.png")));
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid"))
        .thenReturn(Optional.of(new Rank(Tier.EMERALD, Division.I, 11, 5, 2)));
    when(accountPersistencePort.loadAccount("example@email.com"))
        .thenReturn(
            Optional.of(
                new Account(
                    1L,
                    "example@email.com",
                    "Jhonny",
                    "1234",
                    Role.JUNGLE,
                    Region.EUW1,
                    Map.of())));

    Profile profile =
        new FetchProfileService(
                fetchSummonerDataPort,
                fetchLeagueDataPort,
                fetchRiotAccountPort,
                accountPersistencePort)
            .getProfile("example@email.com");

    assertThat(profile.name()).isEqualTo("Jhonny");
    assertThat(profile.tier()).isEqualTo(Tier.EMERALD);
    assertThat(profile.profileIconURI()).isEqualTo("https://ddragon.mock.com/profile.png");
  }

  @Test
  void shouldThrowWhenSummonerNotFound() {
    when(accountPersistencePort.loadAccount("example@email.com"))
        .thenReturn(
            Optional.of(
                new Account(
                    1L,
                    "example@email.com",
                    "fake-name",
                    "fake-tag",
                    Role.JUNGLE,
                    Region.EUW1,
                    Map.of())));
    when(fetchRiotAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                new FetchProfileService(
                        fetchSummonerDataPort,
                        fetchLeagueDataPort,
                        fetchRiotAccountPort,
                        accountPersistencePort)
                    .getProfile("example@email.com"))
        .isInstanceOf(SummonerProfileNotFoundException.class);
  }

  @Test
  void shouldThrowWhenLeagueDataNotFound() {
    when(accountPersistencePort.loadAccount("example@email.com"))
        .thenReturn(
            Optional.of(
                new Account(
                    1L,
                    "example@email.com",
                    "fake-name",
                    "fake-tag",
                    Role.JUNGLE,
                    Region.EUW1,
                    Map.of())));
    when(fetchRiotAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.of("puuid"));
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid")).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                new FetchProfileService(
                        fetchSummonerDataPort,
                        fetchLeagueDataPort,
                        fetchRiotAccountPort,
                        accountPersistencePort)
                    .getProfile("example@email.com"))
        .isInstanceOf(LeagueDataNotFoundException.class);
  }

  @Test
  void shouldThrowWhenSummonerDataNotFound() {
    when(accountPersistencePort.loadAccount("example@email.com"))
        .thenReturn(
            Optional.of(
                new Account(
                    1L,
                    "example@email.com",
                    "fake-name",
                    "fake-tag",
                    Role.JUNGLE,
                    Region.EUW1,
                    Map.of())));
    when(fetchRiotAccountPort.getPuuid("fake-name", "fake-tag")).thenReturn(Optional.of("puuid"));
    when(fetchSummonerDataPort.getSummonerDataByPuuid("puuid")).thenReturn(Optional.empty());
    when(fetchLeagueDataPort.getLeagueDataByPuuid("puuid"))
        .thenReturn(Optional.of(new Rank(Tier.EMERALD, Division.I, 11, 5, 2)));

    assertThatThrownBy(
            () ->
                new FetchProfileService(
                        fetchSummonerDataPort,
                        fetchLeagueDataPort,
                        fetchRiotAccountPort,
                        accountPersistencePort)
                    .getProfile("example@email.com"))
        .isInstanceOf(SummonerDataNotFoundException.class);
  }
}
