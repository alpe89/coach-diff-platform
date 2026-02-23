package com.coachdiff.domain.model;

public record Account(Long id, String email, String name, String tag, Role role, Region region) {}
