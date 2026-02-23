package com.coachdiff.infrastructure.adapter.out.persistence;

import java.io.Serializable;

public record MatchId(String matchId, String puuid) implements Serializable {}
