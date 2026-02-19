package com.coachdiff.application.service;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.LeagueDataNotFoundException;
import com.coachdiff.domain.exception.SummonerDataNotFoundException;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.Profile;
import com.coachdiff.domain.model.RankRecord;
import com.coachdiff.domain.model.SummonerRecord;
import com.coachdiff.domain.port.in.FetchProfilePort;
import com.coachdiff.domain.port.out.FetchAccountPort;
import com.coachdiff.domain.port.out.FetchLeagueDataPort;
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
  private final FetchAccountPort fetchAccountPort;

  FetchProfileService(
      FetchSummonerDataPort fetchSummonerDataPort,
      FetchLeagueDataPort fetchLeagueDataPort,
      FetchAccountPort fetchAccountPort) {
    this.fetchSummonerDataPort = fetchSummonerDataPort;
    this.fetchLeagueDataPort = fetchLeagueDataPort;
    this.fetchAccountPort = fetchAccountPort;
  }

  @Override
  public Profile getProfile(String name, String tag) {
    var puuid =
        fetchAccountPort
            .getPuuid(name, tag)
            .orElseThrow(
                () ->
                    new SummonerProfileNotFoundException(
                        ErrorCode.SUMMONER_NOT_FOUND, "Profile not found for " + name + "#" + tag));

    try (var scope = StructuredTaskScope.open()) {
      StructuredTaskScope.Subtask<Optional<RankRecord>> leagueDataTask =
          scope.fork(() -> fetchLeagueDataPort.getLeagueDataByPuuid(puuid));
      StructuredTaskScope.Subtask<Optional<SummonerRecord>> summonerDataTask =
          scope.fork(() -> fetchSummonerDataPort.getSummonerDataByPuuid(puuid));

      scope.join();

      var leagueData =
          leagueDataTask
              .get()
              .orElseThrow(
                  () ->
                      new LeagueDataNotFoundException(
                          ErrorCode.LEAGUE_DATA_NOT_FOUND,
                          "No league data was found for " + name + "#" + tag));

      var summonerData =
          summonerDataTask
              .get()
              .orElseThrow(
                  () ->
                      new SummonerDataNotFoundException(
                          ErrorCode.SUMMONER_DATA_NOT_FOUND,
                          "No summoner data was found for " + name + "#" + tag));

      return Profile.composeProfile(name, tag, summonerData, leagueData);
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
