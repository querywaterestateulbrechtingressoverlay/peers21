package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("api_campus_data")
public record ApiCampusData(
    @Id
    @Column("id")
    String id,
    @Column("short_name") String shortName,
    @Column("full_name") String fullName
) {
}
