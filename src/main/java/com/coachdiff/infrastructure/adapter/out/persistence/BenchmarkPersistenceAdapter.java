package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.Benchmark;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.model.Tier;
import com.coachdiff.domain.port.out.BenchmarkPersistencePort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BenchmarkPersistenceAdapter implements BenchmarkPersistencePort {
  private final BenchmarkRepository repository;

  public BenchmarkPersistenceAdapter(BenchmarkRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Benchmark> getBenchmark(Role role, Tier tier) {

    return repository.findByTierAndRole(tier.name(), role.name()).map(BenchmarkEntity::toDomain);
  }
}
