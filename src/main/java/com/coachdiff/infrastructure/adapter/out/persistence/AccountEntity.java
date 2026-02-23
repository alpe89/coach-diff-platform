package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Role;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "account")
public class AccountEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String email;
  private String name;
  private String tag;
  private String role;
  private String region;

  protected AccountEntity() {}

  public AccountEntity(Long id, String email, String name, String tag, String role, String region) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.tag = tag;
    this.role = role;
    this.region = region;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getTag() {
    return tag;
  }

  public String getRole() {
    return role;
  }

  public String getRegion() {
    return region;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AccountEntity other)) return false;
    return Objects.equals(id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public static Account toDomain(AccountEntity entity) {
    return new Account(
        entity.getId(),
        entity.getEmail(),
        entity.getName(),
        entity.getTag(),
        Role.valueOf(entity.getRole()),
        Region.valueOf(entity.getRegion()));
  }
}
