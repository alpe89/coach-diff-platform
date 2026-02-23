package com.coachdiff.infrastructure.adapter.out;

import static com.coachdiff.testutil.TestFixtures.createMatchRecord;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.model.Match;
import com.coachdiff.infrastructure.adapter.out.persistence.MatchEntity;
import com.coachdiff.infrastructure.adapter.out.persistence.MatchId;
import com.coachdiff.infrastructure.adapter.out.persistence.MatchPersistenceAdapter;
import com.coachdiff.infrastructure.adapter.out.persistence.MatchRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MatchPersistenceAdapterTest {
  @Mock private MatchRepository repository;

  private MatchPersistenceAdapter matchPersistenceAdapter;

  @BeforeEach
  public void setUp() {
    matchPersistenceAdapter = new MatchPersistenceAdapter(repository);
  }

  @Test
  void shouldSaveMatchRecords() {
    var matchRecordList =
        List.of(
            createMatchRecord("match-1", "puuid"),
            createMatchRecord("match-2", "puuid"),
            createMatchRecord("match-3", "puuid"));

    matchPersistenceAdapter.saveMatchRecords(matchRecordList);

    ArgumentCaptor<List<MatchEntity>> captor = ArgumentCaptor.captor();
    verify(repository).saveAll(captor.capture());

    var savedEntities = captor.getValue();
    assertThat(savedEntities).hasSize(3);
    assertThat(savedEntities.get(0).getId()).isEqualTo(new MatchId("match-1", "puuid"));
    assertThat(savedEntities.get(1).getId()).isEqualTo(new MatchId("match-2", "puuid"));
    assertThat(savedEntities.get(2).getId()).isEqualTo(new MatchId("match-3", "puuid"));
  }

  @Test
  void shouldReturnEmptyListWhenNoMatchRecords() {
    List<Match> emptyList = List.of();

    matchPersistenceAdapter.saveMatchRecords(emptyList);

    ArgumentCaptor<List<MatchEntity>> captor = ArgumentCaptor.captor();
    verify(repository).saveAll(captor.capture());

    var savedEntities = captor.getValue();
    assertThat(savedEntities).isEmpty();
  }

  @Test
  void shouldReturnMatchesQueriedByPuuid() {
    when(repository.findByPuuidAndMatchIdIn("random-puuid", List.of("match-1", "match-2")))
        .thenReturn(
            List.of(
                createMatchRecordEntity("match-1", "random-puuid"),
                createMatchRecordEntity("match-2", "random-puuid")));

    var matchRecords =
        matchPersistenceAdapter.loadExistingMatchRecords(
            "random-puuid", List.of("match-1", "match-2"));

    assertThat(matchRecords).hasSize(2);
    assertThat(matchRecords.get(0).matchId()).isEqualTo("match-1");
    assertThat(matchRecords.get(0).puuid()).isEqualTo("random-puuid");
    assertThat(matchRecords.get(1).matchId()).isEqualTo("match-2");
    assertThat(matchRecords.get(1).puuid()).isEqualTo("random-puuid");
  }

  private MatchEntity createMatchRecordEntity(String matchId, String puuid) {
    return MatchEntity.from(createMatchRecord(matchId, puuid));
  }
}
