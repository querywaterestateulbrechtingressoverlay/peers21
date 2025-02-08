package ru.cyphercola.peers21.backend.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("api_users")
public record ApiUser(
  @Id
  @Column("id")
  Integer id,
  @Column("login")
  String login,
  @Column("password")
  String password,
  @Column("role")
  ApiUserRole role
) {
}
