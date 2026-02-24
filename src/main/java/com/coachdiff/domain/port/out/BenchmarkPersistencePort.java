package com.coachdiff.domain.port.out;

import com.coachdiff.domain.model.Benchmark;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.model.Tier;
import java.util.Optional;

public interface BenchmarkPersistencePort {
  Optional<Benchmark> getBenchmark(Role role, Tier tier);
}
