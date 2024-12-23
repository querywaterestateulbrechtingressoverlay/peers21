package com.example.demo.data;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface ApiCampusDataRepository extends CrudRepository<ApiCampusData, Integer> {
  @Query("SELECT * FROM api_campus_data WHERE short_name = :shortName")
  ApiCampusData findByShortName(String shortName);
}
