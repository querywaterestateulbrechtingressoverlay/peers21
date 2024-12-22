package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("api_campus_data")
public class ApiCampusData implements Persistable<String> {
    private final @Id @Column("id") String id;
    private final @Column("short_name") String shortName;
    private final @Column("full_name") String fullName;

    public ApiCampusData(String id, String shortName, String fullName) {
        this.id = id;
        this.shortName = shortName;
        this.fullName = fullName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    public String getShortName() {
        return shortName;
    }


    public String getFullName() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiCampusData that = (ApiCampusData) o;
        return Objects.equals(id, that.id) && Objects.equals(shortName, that.shortName) && Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shortName, fullName);
    }
}
