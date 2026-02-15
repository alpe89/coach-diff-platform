package com.coachdiff.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.exception.MatchDataNotFoundException;
import com.coachdiff.domain.model.MatchAggregate;
import com.coachdiff.domain.port.out.LoadMatchAggregatePort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FetchMatchAggregateServiceTest {
  @Mock private LoadMatchAggregatePort loadMatchAggregatePort;

  private FetchMatchAggregateService service;

  @BeforeEach
  void setUp() {
    service = new FetchMatchAggregateService(loadMatchAggregatePort);
  }

  @Test
  void shouldReturnMatchAggregate() {
    var aggregate =
        new MatchAggregate(
            10, 6, 4, 5.0, 3.0, 7.0, 4.0, 2.0, 800.0, 1.8, 0.28, 0.20, 0.55, 400.0, 7.5, 75.0,
            3800.0, 5200.0, 5500.0, 3000.0, 7000.0, 2.0, 1.2, 10.0, 3.0, 4.0);
    when(loadMatchAggregatePort.loadMatchAggregate(any(), any()))
        .thenReturn(Optional.of(aggregate));

    var result = service.fetchMatchAggregation("test", "1234");

    assertThat(result.gamesAnalyzed()).isEqualTo(10);
    assertThat(result.wins()).isEqualTo(6);
  }

  @Test
  void shouldThrowWhenNoMatchDataFound() {
    when(loadMatchAggregatePort.loadMatchAggregate(any(), any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.fetchMatchAggregation("test", "1234"))
        .isInstanceOf(MatchDataNotFoundException.class)
        .hasMessageContaining("test#1234");
  }
}
