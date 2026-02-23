package com.coachdiff.infrastructure.adapter.out.riot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.coachdiff.infrastructure.adapter.out.exception.RiotRateLimitException;
import com.coachdiff.infrastructure.config.RiotProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@WireMockTest
class RiotMatchClientTest {
  private RiotMatchClient riotMatchClient;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmInfo) {
    var wmBaseUrl = wmInfo.getHttpBaseUrl();
    var restClientBuilder = RestClient.builder().baseUrl(wmBaseUrl);
    var apiProps =
        new RiotProperties.RiotApi("fake-key", wmBaseUrl, wmBaseUrl, wmBaseUrl, wmBaseUrl);
    var ddragonProps = new RiotProperties.RiotDdragon("https://ddragon-mock.com/cdn", "16.3.1");
    var rateLimitProps = new RiotProperties.RiotRateLimit(20, 5);
    var riotProperties = new RiotProperties(apiProps, ddragonProps, rateLimitProps);
    var rateLimiter = RateLimiter.of("test", RateLimiterConfig.custom().build());

    this.riotMatchClient =
        new RiotMatchClient(restClientBuilder, riotProperties, rateLimiter, 1736294400L);
  }

  @Test
  void shouldGetMatchIds() {
    stubFor(
        get(urlPathEqualTo("/lol/match/v5/matches/by-puuid/test-puuid/ids"))
            .withQueryParam("startTime", equalTo("1736294400"))
            .willReturn(
                okJson(
                    """
                    ["EUW1_1111", "EUW1_2222", "EUW1_3333"]
                    """)));

    var matchIds = riotMatchClient.getMatchesIds("test-puuid");

    assertThat(matchIds).containsExactly("EUW1_1111", "EUW1_2222", "EUW1_3333");
  }

  @Test
  void shouldGetMatchData() {
    stubFor(
        get(urlPathEqualTo("/lol/match/v5/matches/EUW1_1111"))
            .willReturn(
                okJson(
                    """
                    {
                      "info": {
                        "gameDuration": 1800,
                        "participants": [
                          {
                            "puuid": "my-puuid",
                            "win": true,
                            "kills": 10,
                            "deaths": 2,
                            "assists": 5,
                            "totalDamageDealtToChampions": 25000,
                            "goldEarned": 12000,
                            "damageDealtToTurrets": 3000,
                            "damageDealtToObjectives": 8000,
                            "totalMinionsKilled": 180,
                            "neutralMinionsKilled": 20,
                            "wardsPlaced": 12,
                            "wardsKilled": 4,
                            "challenges": {
                              "kda": 7.5,
                              "soloKills": 3,
                              "damagePerMinute": 833.3,
                              "teamDamagePercentage": 0.28,
                              "damageTakenOnTeamPercentage": 0.20,
                              "killParticipation": 0.60,
                              "goldPerMinute": 400.0,
                              "visionScorePerMinute": 1.5,
                              "controlWardsPlaced": 5,
                              "turretPlatesTaken": 2
                            }
                          }
                        ]
                      }
                    }
                    """)));

    var match = riotMatchClient.getMatchData("EUW1_1111");

    assertThat(match.info().gameDuration()).isEqualTo(1800);
    assertThat(match.info().participants()).hasSize(1);

    var participant = match.info().participants().getFirst();
    assertThat(participant.puuid()).isEqualTo("my-puuid");
    assertThat(participant.win()).isTrue();
    assertThat(participant.kills()).isEqualTo(10);
    assertThat(participant.challenges().kda()).isEqualTo(7.5);
  }

  @Test
  void shouldGetMatchTimelineData() {
    stubFor(
        get(urlPathEqualTo("/lol/match/v5/matches/EUW1_1111/timeline"))
            .willReturn(
                okJson(
                    """
                    {
                      "metadata": {
                        "participants": ["puuid-1", "puuid-2"]
                      },
                      "info": {
                        "frameInterval": 60000,
                        "frames": [
                          {
                            "timestamp": 0,
                            "participantFrames": {
                              "1": { "minionsKilled": 0, "jungleMinionsKilled": 0, "totalGold": 500, "xp": 0 },
                              "2": { "minionsKilled": 0, "jungleMinionsKilled": 0, "totalGold": 500, "xp": 0 }
                            }
                          }
                        ]
                      }
                    }
                    """)));

    var timeline = riotMatchClient.getMatchTimelineData("EUW1_1111");

    assertThat(timeline.metadata().participants()).containsExactly("puuid-1", "puuid-2");
    assertThat(timeline.info().frameInterval()).isEqualTo(60000);
    assertThat(timeline.info().frames()).hasSize(1);

    var frame = timeline.info().frames().getFirst();
    assertThat(frame.participantFrames()).containsKey("1");
    assertThat(frame.participantFrames().get("1").totalGold()).isEqualTo(500);
  }

  @Test
  void shouldThrowWhenRiotReturnsRateLimitOnMatchIds() {
    stubFor(
        get(urlPathEqualTo("/lol/match/v5/matches/by-puuid/test-puuid/ids"))
            .willReturn(aResponse().withStatus(429)));

    assertThatThrownBy(() -> riotMatchClient.getMatchesIds("test-puuid"))
        .isInstanceOf(RiotRateLimitException.class);
  }

  @Test
  void shouldThrowWhenRiotReturnsRateLimitOnMatchData() {
    stubFor(
        get(urlPathEqualTo("/lol/match/v5/matches/EUW1_9999"))
            .willReturn(aResponse().withStatus(429)));

    assertThatThrownBy(() -> riotMatchClient.getMatchData("EUW1_9999"))
        .isInstanceOf(RiotRateLimitException.class);
  }
}
