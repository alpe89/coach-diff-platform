package com.coachdiff.domain.model;

import java.util.Map;

public record Account(
    Long id,
    String email,
    String name,
    String tag,
    Role role,
    Region region,
    Map<Permission, Boolean> permissions) {

  public Account withUpdates(String name, String tag, Role role, Region region) {
    return new Account(this.id, this.email, name, tag, role, region, this.permissions);
  }
}
