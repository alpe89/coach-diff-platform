package com.coachdiff.infrastructure.adapter.out.riot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Rank;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.infrastructure.config.RiotProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@WireMockTest
public class RiotLeagueDataAdapterTest {
  private RiotLeagueDataAdapter adapter;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmInfo) {
    var wmBaseUrl = wmInfo.getHttpBaseUrl();

    var riotProperties =
        new RiotProperties(
            new RiotProperties.RiotApi("fake-key", wmBaseUrl, wmBaseUrl, wmBaseUrl, wmBaseUrl),
            new RiotProperties.RiotDdragon("https://ddragon.mock.com", "16.3.1"),
            new RiotProperties.RiotRateLimit(20, 1));

    var client = new RiotLeagueClient(RestClient.builder(), riotProperties);

    this.adapter = new RiotLeagueDataAdapter(client);

    // Stub

    stubFor(
        get(urlPathEqualTo("/lol/league/v4/entries/by-puuid/real-puuid"))
            .willReturn(
                okJson(
                    """
                    [
                        {
                            "leagueId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                            "queueType": "RANKED_SOLO_5x5",
                            "tier": "GOLD",
                            "rank": "I",
                            "puuid": "real-puuid",
                            "leaguePoints": 30,
                            "wins": 7,
                            "losses": 6,
                            "veteran": false,
                            "inactive": false,
                            "freshBlood": false,
                            "hotStreak": false
                        }
                    ]
                    """)));

    stubFor(
        get(urlPathEqualTo("/lol/league/v4/entries/by-puuid/not-found-puuid"))
            .willReturn(notFound()));
  }

  @Test
  void shouldReturnRankRecordWithExistingPuuid() {
    var leagueData = adapter.getLeagueDataByPuuid("real-puuid");

    assertThat(leagueData)
        .isPresent()
        .get()
        .extracting(Rank::division, Rank::tier, Rank::lp, Rank::wins, Rank::losses)
        .containsExactly(Division.I, Tier.GOLD, 30, 7, 6);
  }

  @Test
  void shouldReturnEmptyOptionalWhenPuuidIsNotFound() {
    var leagueData = adapter.getLeagueDataByPuuid("not-found-puuid");
    assertThat(leagueData).isEmpty();
  }
}
