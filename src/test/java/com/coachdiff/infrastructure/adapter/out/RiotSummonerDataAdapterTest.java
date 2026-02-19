package com.coachdiff.infrastructure.adapter.out;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.coachdiff.domain.model.SummonerRecord;
import com.coachdiff.infrastructure.config.RiotProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@WireMockTest
public class RiotSummonerDataAdapterTest {
  private RiotSummonerDataAdapter adapter;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmInfo) {

    var wmBaseUrl = wmInfo.getHttpBaseUrl();
    var riotProps =
        new RiotProperties(
            new RiotProperties.RiotApi("fake-key", wmBaseUrl, wmBaseUrl, wmBaseUrl, wmBaseUrl),
            new RiotProperties.RiotDdragon("https://ddragon.mock.com", "16.3.1"),
            new RiotProperties.RiotRateLimit(20, 1));

    var riotClient = new RiotSummonerClient(RestClient.builder(), riotProps);

    this.adapter = new RiotSummonerDataAdapter(riotClient, riotProps);

    // Stubs

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

    stubFor(get(urlPathEqualTo("/lol/summoner/v4/summoners/by-puuid/404")).willReturn(notFound()));
  }

  @Test
  void shouldGetSummonerDataWhenPuuidIsFound() {
    var summonerData = adapter.getSummonerDataByPuuid("abc-def-ghi");

    assertThat(summonerData)
        .isPresent()
        .get()
        .extracting(SummonerRecord::profileIconURI)
        .isEqualTo("https://ddragon.mock.com/16.3.1/img/profileicon/6762.png");
  }

  @Test
  void shouldReturnEmptyOptionalWhenPuuidIsNotFound() {
    var summonerData = adapter.getSummonerDataByPuuid("404");
    assertThat(summonerData).isEmpty();
  }
}
