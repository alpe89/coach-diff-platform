package com.coachdiff.domain.model;

public record Benchmark(
    Tier tier,
    Role role,
    Double medianCsPerMinute,
    Double medianKda,
    Double medianGoldPerMinute,
    Double medianDamagePerMinute,
    Double medianVisionScorePerMinute,
    Double medianKillParticipation,
    Double averageCsPerMinute,
    Double averageKda,
    Double averageGoldPerMinute,
    Double averageDamagePerMinute,
    Double averageVisionScorePerMinute,
    Double averageKillParticipation) {}
