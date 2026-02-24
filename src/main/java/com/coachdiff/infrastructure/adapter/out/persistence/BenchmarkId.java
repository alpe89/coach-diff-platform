package com.coachdiff.infrastructure.adapter.out.persistence;

import java.io.Serializable;

public record BenchmarkId(String tier, String role) implements Serializable {}
