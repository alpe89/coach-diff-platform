package com.coachdiff.infrastructure.adapter.in.rest.dto;

import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Role;

public record UpdateAccountRequestDto(String name, String tag, Role coachingRole, Region region) {
  public static Account toAccount(Long id, String email, UpdateAccountRequestDto dto) {
    return new Account(id, email, dto.name(), dto.tag(), dto.coachingRole(), dto.region());
  }
}
