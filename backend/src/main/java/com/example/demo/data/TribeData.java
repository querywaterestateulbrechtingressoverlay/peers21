package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("tribe_data")
public record TribeData(
  @Id
  @Column("id")
  Integer id,
  @Column("tribe_id")
  int tribeId,
  @Column("name")
  String name
) {
}
