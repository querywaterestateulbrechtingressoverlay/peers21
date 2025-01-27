package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("intensive_data")
public record IntensiveData(
  @Id
  @Column("id")
  Integer id,
  @Column("start_date")
  Date startDate,
  @Column("end_date")
  Date endDate
) {}
