package com.example.demo.data;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PeerBaseDataRepository extends CrudRepository<PeerBaseData, Integer>, PagingAndSortingRepository<PeerBaseData, Integer> {
  @Query("SELECT login FROM peer_base_data")
  List<String> getAllLogins();
}
