package com.coachdiff.infrastructure.adapter.out.persistence;

import java.io.Serializable;

public record MatchRecordId(String matchId, String puuid) implements Serializable {}
