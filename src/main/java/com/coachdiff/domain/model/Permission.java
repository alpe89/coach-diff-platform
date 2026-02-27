package com.coachdiff.domain.model;

import java.util.HashMap;
import java.util.Map;

public enum Permission {
  BASE_USE("base_use"),
  COACH_USE("coach_use");

  private final String key;
  private static final Map<String, Permission> KEY_LOOKUP = new HashMap<>();

  static {
    for (Permission permission : Permission.values()) {
      KEY_LOOKUP.put(permission.getKey(), permission);
    }
  }

  Permission(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public static Permission fromKey(String key) {
    var permission = KEY_LOOKUP.get(key);

    if (permission == null) {
      throw new IllegalArgumentException("Invalid permission key: " + key);
    }

    return permission;
  }
}
