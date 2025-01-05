package com.example.demo.data;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;

public interface IntensiveDatesRepository extends CrudRepository<IntensiveDates, Integer> {
  @Query("SELECT COALESCE((SELECT id FROM intensives WHERE :firstXpAccrualDate BETWEEN start_date AND end_date), 0)")
  Integer findIntensiveByFirstXpAccrualDate(Date firstXpAccrualDate);
}
