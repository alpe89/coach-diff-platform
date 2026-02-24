package com.coachdiff.infrastructure.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.infrastructure.adapter.out.persistence.BenchmarkEntity;
import com.coachdiff.infrastructure.adapter.out.persistence.BenchmarkPersistenceAdapter;
import com.coachdiff.infrastructure.adapter.out.persistence.BenchmarkRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BenchmarkPersistenceAdapterTest {
  @Mock private BenchmarkRepository repository;

  private BenchmarkPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new BenchmarkPersistenceAdapter(repository);
  }

  @Test
  void shouldReturnBenchmarkForTierAndRole() {
    when(repository.findByTierAndRole("GOLD", "ADC"))
        .thenReturn(Optional.of(createBenchmarkEntity()));

    var result = adapter.getBenchmark(Role.ADC, Tier.GOLD);

    assertThat(result).isPresent();
    var benchmark = result.get();
    assertThat(benchmark.tier()).isEqualTo(Tier.GOLD);
    assertThat(benchmark.role()).isEqualTo(Role.ADC);
    assertThat(benchmark.medianCsPerMinute()).isEqualTo(7.2);
    assertThat(benchmark.medianKda()).isEqualTo(2.8);
    assertThat(benchmark.medianGoldPerMinute()).isEqualTo(380.0);
    assertThat(benchmark.medianDamagePerMinute()).isEqualTo(620.0);
    assertThat(benchmark.medianVisionScorePerMinute()).isEqualTo(0.9);
    assertThat(benchmark.medianKillParticipation()).isEqualTo(0.58);
    assertThat(benchmark.averageCsPerMinute()).isEqualTo(7.5);
    assertThat(benchmark.averageKda()).isEqualTo(3.0);
    assertThat(benchmark.averageGoldPerMinute()).isEqualTo(400.0);
    assertThat(benchmark.averageDamagePerMinute()).isEqualTo(650.0);
    assertThat(benchmark.averageVisionScorePerMinute()).isEqualTo(1.0);
    assertThat(benchmark.averageKillParticipation()).isEqualTo(0.60);
  }

  @Test
  void shouldReturnEmptyWhenBenchmarkNotFound() {
    when(repository.findByTierAndRole("CHALLENGER", "SUPPORT")).thenReturn(Optional.empty());

    var result = adapter.getBenchmark(Role.SUPPORT, Tier.CHALLENGER);

    assertThat(result).isEmpty();
  }

  static BenchmarkEntity createBenchmarkEntity() {
    return new BenchmarkEntity(
        "GOLD", "ADC", 7.2, 2.8, 380.0, 620.0, 0.9, 0.58, 7.5, 3.0, 400.0, 650.0, 1.0, 0.60);
  }
}
