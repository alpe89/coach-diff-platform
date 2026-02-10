package com.coachdiff.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.domain.model.Division;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.SummonerProfile;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.out.LoadProfileDataPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchProfileServiceTest {
  @Mock private LoadProfileDataPort loadProfileDataPort;

  @BeforeEach
  void setUp() {
    when(loadProfileDataPort.loadProfileData(any(), any()))
        .thenReturn(
            Optional.of(
                new SummonerProfile(
                    "Summoner",
                    "Tag",
                    Region.EUW1,
                    "https://ddragon.url",
                    Tier.EMERALD,
                    Division.I,
                    11,
                    5,
                    2)));
  }

  @Test
  void shouldGetSummonerProfile() {
    SummonerProfile profile =
        new FetchProfileService(loadProfileDataPort).getSummonerProfile("aaa", "bbb");
    assertThat(profile.name()).isEqualTo("Summoner");
    assertThat(profile.tier()).isEqualTo(Tier.EMERALD);
  }

  @Test
  void shouldThrowWhenProfileNotFound() {
    when(loadProfileDataPort.loadProfileData(any(), any())).thenReturn(Optional.empty());
    assertThrows(
        SummonerProfileNotFoundException.class,
        () -> new FetchProfileService(loadProfileDataPort).getSummonerProfile("aaa", "bbb"));
  }
}
