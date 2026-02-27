package com.coachdiff.infrastructure.adapter.out.persistence;

import com.coachdiff.domain.model.Account;
import com.coachdiff.domain.model.Permission;
import com.coachdiff.domain.model.Region;
import com.coachdiff.domain.model.Role;
import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Map<String, Boolean> permissions;

  protected AccountEntity() {}

  public AccountEntity(
      Long id,
      String email,
      String name,
      String tag,
      String role,
      String region,
      Map<String, Boolean> permissions) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.tag = tag;
    this.role = role;
    this.region = region;
    this.permissions = permissions;
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

  public Map<String, Boolean> getPermissions() {
    return permissions;
  }

  public Boolean hasPermission(String permission) {
    return permissions.getOrDefault(permission, false);
  }

  public void setPermissions(Map<String, Boolean> permissions) {
    this.permissions = permissions;
  }

  public void setPermission(String permission, Boolean value) {
    permissions.put(permission, value);
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
    Map<Permission, Boolean> permissions = new HashMap<>();

    for (Map.Entry<String, Boolean> entry : entity.getPermissions().entrySet()) {
      permissions.put(Permission.fromKey(entry.getKey()), entry.getValue());
    }

    return new Account(
        entity.getId(),
        entity.getEmail(),
        entity.getName(),
        entity.getTag(),
        Role.valueOf(entity.getRole()),
        Region.valueOf(entity.getRegion()),
        permissions);
  }

  public static AccountEntity fromDomain(Account account) {
    Map<String, Boolean> permissions = new HashMap<>();

    for (Map.Entry<Permission, Boolean> entry : account.permissions().entrySet()) {
      permissions.put(entry.getKey().getKey(), entry.getValue());
    }

    return new AccountEntity(
        account.id(),
        account.email(),
        account.name(),
        account.tag(),
        account.role().name(),
        account.region().name(),
        permissions);
  }
}
