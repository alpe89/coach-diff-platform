package com.coachdiff.infrastructure.adapter.out;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.coachdiff.domain.model.SummonerProfile;
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
        get(urlPathEqualTo("/riot/account/v1/accounts/by-riot-id/not/found"))
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

    RestClient.Builder riotRestClient = RestClient.builder().baseUrl(wmInfo.getHttpBaseUrl());
    this.profileDataAdapter =
        new ProfileDataAdapter(riotRestClient, wmInfo.getHttpBaseUrl(), wmInfo.getHttpBaseUrl());
  }

  @Test
  void shouldGetProfileDataWhenCorrectSummonerNameAndTagIsProvided() {

    Optional<SummonerProfile> profile = profileDataAdapter.loadProfileData("test", "1234");
    assertThat(profile)
        .isPresent()
        .get()
        .extracting(SummonerProfile::name, SummonerProfile::tag, SummonerProfile::gamesPlayed)
        .containsExactly("test", "1234", 6);
  }

  @Test
  void shouldGetAnEmptyOptionalWhenSummonerNameOrTagIsInvalid() {
    Optional<SummonerProfile> profile = profileDataAdapter.loadProfileData("not", "found");
    assertThat(profile).isEmpty();
  }
}
