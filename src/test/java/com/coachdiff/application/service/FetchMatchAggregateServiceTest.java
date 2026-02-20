package com.coachdiff.application.service;

import static com.coachdiff.testutil.TestFixtures.createMatchRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.coachdiff.domain.port.out.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchMatchAggregateServiceTest {
  @Mock private SaveMatchRecordsPort saveMatchRecordsPort;
  @Mock private LoadMatchRecordsPort loadMatchRecordsPort;
  @Mock private FetchAccountPort fetchAccountPort;
  @Mock private FetchMatchDetailsPort fetchMatchDetailsPort;

  private FetchMatchAggregateService service;
  private final String name = "test";
  private final String tag = "1234";

  @BeforeEach
  void setUp() {
    service =
        new FetchMatchAggregateService(
            fetchAccountPort, fetchMatchDetailsPort, loadMatchRecordsPort, saveMatchRecordsPort);
  }

  @Test
  void shouldReturnMatchAggregate() {
    when(fetchAccountPort.getPuuid(name, tag)).thenReturn(Optional.of("fake-puuid"));

    when(fetchMatchDetailsPort.getMatchIdsByPuuid("fake-puuid"))
        .thenReturn(List.of("EUW1_1111", "EUW1_1112"));

    when(loadMatchRecordsPort.loadExistingMatchRecords(
            "fake-puuid", List.of("EUW1_1111", "EUW1_1112")))
        .thenReturn(List.of(createMatchRecord("EUW1_1111", "fake-puuid")));

    when(fetchMatchDetailsPort.getMatchRecords("fake-puuid", List.of("EUW1_1112")))
        .thenReturn(List.of(createMatchRecord("EUW1_1112", "fake-puuid")));

    var result = service.fetchMatchAggregation(name, tag);

    verify(fetchAccountPort).getPuuid(name, tag);
    verify(fetchMatchDetailsPort).getMatchIdsByPuuid("fake-puuid");
    verify(loadMatchRecordsPort)
        .loadExistingMatchRecords("fake-puuid", List.of("EUW1_1111", "EUW1_1112"));
    verify(fetchMatchDetailsPort).getMatchRecords("fake-puuid", List.of("EUW1_1112"));
    verify(saveMatchRecordsPort)
        .saveMatchRecords(List.of(createMatchRecord("EUW1_1112", "fake-puuid")));

    assertThat(result.gamesAnalyzed()).isEqualTo(2);
    assertThat(result.wins()).isEqualTo(2);
  }

  @Test
  void shouldFilterOutRemakes() {
    when(fetchAccountPort.getPuuid(name, tag)).thenReturn(Optional.of("fake-puuid"));
    when(fetchMatchDetailsPort.getMatchIdsByPuuid("fake-puuid"))
        .thenReturn(List.of("EUW1_2001", "EUW1_2002"));
    when(loadMatchRecordsPort.loadExistingMatchRecords(
            "fake-puuid", List.of("EUW1_2001", "EUW1_2002")))
        .thenReturn(List.of());
    when(fetchMatchDetailsPort.getMatchRecords("fake-puuid", List.of("EUW1_2001", "EUW1_2002")))
        .thenReturn(
            List.of(
                createMatchRecord("EUW1_2001", "fake-puuid", 25.0),
                createMatchRecord("EUW1_2002", "fake-puuid", 3.0)));

    var result = service.fetchMatchAggregation(name, tag);

    verify(saveMatchRecordsPort)
        .saveMatchRecords(List.of(createMatchRecord("EUW1_2001", "fake-puuid", 25.0)));
    assertThat(result.gamesAnalyzed()).isEqualTo(1);
  }

  @Test
  void shouldNotSaveWhenAllMatchesAreRemakes() {
    when(fetchAccountPort.getPuuid(name, tag)).thenReturn(Optional.of("fake-puuid"));
    when(fetchMatchDetailsPort.getMatchIdsByPuuid("fake-puuid")).thenReturn(List.of("EUW1_3001"));
    when(loadMatchRecordsPort.loadExistingMatchRecords("fake-puuid", List.of("EUW1_3001")))
        .thenReturn(List.of());
    when(fetchMatchDetailsPort.getMatchRecords("fake-puuid", List.of("EUW1_3001")))
        .thenReturn(List.of(createMatchRecord("EUW1_3001", "fake-puuid", 2.5)));

    var result = service.fetchMatchAggregation(name, tag);

    verifyNoInteractions(saveMatchRecordsPort);
    assertThat(result.gamesAnalyzed()).isZero();
  }
}
