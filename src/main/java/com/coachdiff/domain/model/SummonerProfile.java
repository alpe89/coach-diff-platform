package com.coachdiff.domain.model;

public record SummonerProfile(
    String name,
    String tag,
    Region region,
    Tier tier,
    Division division,
    int lp,
    int wins,
    int losses) {}
