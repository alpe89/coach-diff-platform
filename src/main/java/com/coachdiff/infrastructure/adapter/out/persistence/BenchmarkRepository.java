package com.coachdiff.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenchmarkRepository extends JpaRepository<BenchmarkEntity, BenchmarkId> {
  Optional<BenchmarkEntity> findByTierAndRole(String tier, String role);
}
