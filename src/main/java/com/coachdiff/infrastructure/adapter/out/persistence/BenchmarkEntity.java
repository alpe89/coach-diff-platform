package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.Benchmark;
import com.coachdiff.domain.model.Role;
import com.coachdiff.domain.model.Tier;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "benchmark")
@IdClass(BenchmarkId.class)
public class BenchmarkEntity {

  @Id private String tier;
  @Id private String role;

  private double medianCsPerMin;
  private double medianKda;
  private double medianGoldPerMin;
  private double medianDpm;
  private double medianVisionScorePerMin;
  private double medianKillParticipation;

  private double avgCsPerMin;
  private double avgKda;
  private double avgGoldPerMin;
  private double avgDpm;
  private double avgVisionScorePerMin;
  private double avgKillParticipation;

  protected BenchmarkEntity() {}

  public BenchmarkEntity(
      String tier,
      String role,
      double medianCsPerMin,
      double medianKda,
      double medianGoldPerMin,
      double medianDpm,
      double medianVisionScorePerMin,
      double medianKillParticipation,
      double avgCsPerMin,
      double avgKda,
      double avgGoldPerMin,
      double avgDpm,
      double avgVisionScorePerMin,
      double avgKillParticipation) {
    this.tier = tier;
    this.role = role;
    this.medianCsPerMin = medianCsPerMin;
    this.medianKda = medianKda;
    this.medianGoldPerMin = medianGoldPerMin;
    this.medianDpm = medianDpm;
    this.medianVisionScorePerMin = medianVisionScorePerMin;
    this.medianKillParticipation = medianKillParticipation;
    this.avgCsPerMin = avgCsPerMin;
    this.avgKda = avgKda;
    this.avgGoldPerMin = avgGoldPerMin;
    this.avgDpm = avgDpm;
    this.avgVisionScorePerMin = avgVisionScorePerMin;
    this.avgKillParticipation = avgKillParticipation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BenchmarkEntity other)) return false;
    return Objects.equals(tier, other.tier) && Objects.equals(role, other.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tier, role);
  }

  public static Benchmark toDomain(BenchmarkEntity entity) {
    return new Benchmark(
        Tier.valueOf(entity.tier),
        Role.valueOf(entity.role),
        entity.medianCsPerMin,
        entity.medianKda,
        entity.medianGoldPerMin,
        entity.medianDpm,
        entity.medianVisionScorePerMin,
        entity.medianKillParticipation,
        entity.avgCsPerMin,
        entity.avgKda,
        entity.avgGoldPerMin,
        entity.avgDpm,
        entity.avgVisionScorePerMin,
        entity.avgKillParticipation);
  }
}
