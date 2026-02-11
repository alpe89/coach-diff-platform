package com.coachdiff.infrastructure.adapter.out;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.infrastructure.adapter.out.exception.RiotAuthenticationException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotRateLimitException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotUnknownException;
import com.coachdiff.infrastructure.config.RiotProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@WireMockTest
class ProfileDataAdapterTest {
  private ProfileDataAdapter profileDataAdapter;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmInfo) {
    stubFor(
        get(urlPathEqualTo("/riot/account/v1/accounts/by-riot-id/test/1234"))
            .willReturn(
                okJson(
                    """
                    { "puuid": "abc-def-ghi", "gameName": "test", "tagLine": "1234" }
                    """)));

    stubFor(
        get(urlPathEqualTo("/riot/account/v1/accounts/by-riot-id/api/error"))
            .willReturn(
                okJson(
                    """
                    { "puuid": "puuid-api-error", "gameName": "test", "tagLine": "1234" }
                    """)));

    stubFor(
        get(urlPathEqualTo("/riot/account/v1/accounts/by-riot-id/not/found"))
            .willReturn(notFound()));

    stubFor(
        get(urlPathEqualTo("/riot/account/v1/accounts/by-riot-id/unexpected/error"))
            .willReturn(badRequest()));

    stubFor(
        get(urlPathEqualTo("/riot/account/v1/accounts/by-riot-id/rate/limit"))
            .willReturn(aResponse().withStatus(429)));

    stubFor(
        get(urlPathEqualTo("/lol/summoner/v4/summoners/by-puuid/abc-def-ghi"))
            .willReturn(
                okJson(
                    """
                    {
                        "puuid": "abc-def-ghi",
                        "profileIconId": 6762,
                        "revisionDate": 1770672424000,
                        "summonerLevel": 302
                    }
                    """)));

    stubFor(
        get(urlPathEqualTo("/lol/summoner/v4/summoners/by-puuid/puuid-api-error"))
            .willReturn(aResponse().withStatus(403)));

    stubFor(
        get(urlPathEqualTo("/lol/summoner/v4/summoners/by-puuid/not/found"))
            .willReturn(notFound()));

    stubFor(
        get(urlPathEqualTo("/lol/league/v4/entries/by-puuid/abc-def-ghi"))
            .willReturn(
                okJson(
                    """
                    [
                        {
                            "leagueId": "----------------",
                            "queueType": "RANKED_SOLO_5x5",
                            "tier": "SILVER",
                            "rank": "III",
                            "puuid": "abc-def-ghi",
                            "leaguePoints": 40,
                            "wins": 3,
                            "losses": 3,
                            "veteran": false,
                            "inactive": false,
                            "freshBlood": false,
                            "hotStreak": false
                        }
                    ]\
                    """)));

    stubFor(
        get(urlPathEqualTo("/lol/league/v4/entries/by-puuid/puuid-api-error"))
            .willReturn(
                okJson(
                    """
                    [
                        {
                            "leagueId": "----------------",
                            "queueType": "RANKED_SOLO_5x5",
                            "tier": "SILVER",
                            "rank": "III",
                            "puuid": "abc-def-ghi",
                            "leaguePoints": 40,
                            "wins": 3,
                            "losses": 3,
                            "veteran": false,
                            "inactive": false,
                            "freshBlood": false,
                            "hotStreak": false
                        }
                    ]\
                    """)));

    var wmBaseUrl = wmInfo.getHttpBaseUrl();

    var riotRestClient = RestClient.builder().baseUrl(wmBaseUrl);
    var apiProps =
        new RiotProperties.RiotApi("fake-key", wmBaseUrl, wmBaseUrl, wmBaseUrl, wmBaseUrl);
    var ddragonProps = new RiotProperties.RiotDdragon("https://ddragon-mock.com/cdn", "16.3.1");

    var riotProperties = new RiotProperties(apiProps, ddragonProps);

    this.profileDataAdapter = new ProfileDataAdapter(riotRestClient, riotProperties);
  }

  @Test
  void shouldGetProfileDataWhenCorrectSummonerNameAndTagIsProvided() {

    Optional<SummonerProfile> profile = profileDataAdapter.loadProfileData("test", "1234");
    assertThat(profile)
        .isPresent()
        .get()
        .extracting(
            SummonerProfile::name,
            SummonerProfile::tag,
            SummonerProfile::gamesPlayed,
            SummonerProfile::profileIconURI)
        .containsExactly(
            "test", "1234", 6, "https://ddragon-mock.com/cdn/16.3.1/img/profileicon/6762.png");
  }

  @Test
  void shouldGetAnEmptyOptionalWhenSummonerNameOrTagIsInvalid() {
    Optional<SummonerProfile> profile = profileDataAdapter.loadProfileData("not", "found");
    assertThat(profile).isEmpty();
  }

  @Test
  void shouldThrowWhenRateLimitIsReached() {
    assertThatThrownBy(() -> profileDataAdapter.loadProfileData("rate", "limit"))
        .isInstanceOf(RiotRateLimitException.class)
        .message()
        .isEqualTo("Encountered a rate limit issue with the Riot API");
  }

  @Test
  void shouldThrowWhenRiotApiReturnsAnError() {
    assertThatThrownBy(() -> profileDataAdapter.loadProfileData("api", "error"))
        .isInstanceOf(RiotAuthenticationException.class)
        .message()
        .isEqualTo("Encountered a problem likely with the Riot API key");
  }

  @Test
  void shouldThrowWhenRiotApiReturnsAnUnexpectedError() {
    assertThatThrownBy(() -> profileDataAdapter.loadProfileData("unexpected", "error"))
        .isInstanceOf(RiotUnknownException.class)
        .message()
        .isEqualTo("Unknown error on the Riot API");
  }
}
