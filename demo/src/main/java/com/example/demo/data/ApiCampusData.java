package com.example.demo.data;

import org.springframework.data.relational.core.mapping.Column;

public record ApiCampusData(
    int id,
    @Column("short_name") String shortName,
    @Column("full_name") String fullName
) {
}
