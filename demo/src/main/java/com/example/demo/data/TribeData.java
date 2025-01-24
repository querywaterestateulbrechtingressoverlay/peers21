package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

public record TribeData(
  @Id
  @Column("id") int id,
  @Column("name") String name
) {
}
