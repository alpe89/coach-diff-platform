package com.coachdiff.application.service;

import com.coachdiff.domain.exception.*;
import com.coachdiff.domain.model.Profile;
import com.coachdiff.domain.model.Rank;
import com.coachdiff.domain.model.Summoner;
import com.coachdiff.domain.port.in.FetchProfilePort;
import com.coachdiff.domain.port.out.AccountPersistencePort;
import com.coachdiff.domain.port.out.FetchLeagueDataPort;
import com.coachdiff.domain.port.out.FetchRiotAccountPort;
import com.coachdiff.domain.port.out.FetchSummonerDataPort;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FetchProfileService implements FetchProfilePort {
  private static final Logger log = LoggerFactory.getLogger(FetchProfileService.class);
  private final FetchLeagueDataPort fetchLeagueDataPort;
  private final FetchSummonerDataPort fetchSummonerDataPort;
  private final FetchRiotAccountPort fetchRiotAccountPort;
  private final AccountPersistencePort accountPersistencePort;

  FetchProfileService(
      FetchSummonerDataPort fetchSummonerDataPort,
      FetchLeagueDataPort fetchLeagueDataPort,
      FetchRiotAccountPort fetchRiotAccountPort,
      AccountPersistencePort accountPersistencePort) {
    this.fetchSummonerDataPort = fetchSummonerDataPort;
    this.fetchLeagueDataPort = fetchLeagueDataPort;
    this.fetchRiotAccountPort = fetchRiotAccountPort;
    this.accountPersistencePort = accountPersistencePort;
  }

  @Override
  public Profile getProfile(String email) {
    var account =
        accountPersistencePort
            .loadAccount(email)
            .orElseThrow(
                () ->
                    new AccountNotFoundException(
                        ErrorCode.ACCOUNT_DATA_NOT_FOUND, "Account data not found for " + email));

    var puuid =
        fetchRiotAccountPort
            .getPuuid(account.name(), account.tag())
            .orElseThrow(
                () ->
                    new SummonerProfileNotFoundException(
                        ErrorCode.SUMMONER_NOT_FOUND,
                        "Profile not found for " + account.name() + "#" + account.tag()));

    try (var scope = StructuredTaskScope.open()) {
      StructuredTaskScope.Subtask<Optional<Rank>> leagueDataTask =
          scope.fork(() -> fetchLeagueDataPort.getLeagueDataByPuuid(puuid));
      StructuredTaskScope.Subtask<Optional<Summoner>> summonerDataTask =
          scope.fork(() -> fetchSummonerDataPort.getSummonerDataByPuuid(puuid));

      scope.join();

      var leagueData =
          leagueDataTask
              .get()
              .orElseThrow(
                  () ->
                      new LeagueDataNotFoundException(
                          ErrorCode.LEAGUE_DATA_NOT_FOUND,
                          "No league data was found for " + account.name() + "#" + account.tag()));

      var summonerData =
          summonerDataTask
              .get()
              .orElseThrow(
                  () ->
                      new SummonerDataNotFoundException(
                          ErrorCode.SUMMONER_DATA_NOT_FOUND,
                          "No summoner data was found for "
                              + account.name()
                              + "#"
                              + account.tag()));

      return Profile.composeProfile(account.name(), account.tag(), summonerData, leagueData);
    } catch (InterruptedException e) {
      log.error("Thread interrupted while fetching profile data", e);
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (StructuredTaskScope.FailedException e) {
      if (e.getCause() instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException(e.getCause());
    }
  }
}
