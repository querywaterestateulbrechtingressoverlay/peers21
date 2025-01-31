package com.example.demo.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PeerBaseDataRepository extends CrudRepository<PeerBaseData, Integer>, PagingAndSortingRepository<PeerBaseData, Integer> {
  @Query("SELECT login FROM peer_base_data")
  List<String> getAllLogins();
  @Query("SELECT DISTINCT wave FROM peer_base_data")
  List<String> findDistinctWaves();
  @Query("SELECT DISTINCT tribe_data.id, tribe_data.name FROM peer_base_data JOIN tribe_data ON peer_base_data.tribe_id = tribe_data.id")
  List<TribeData> findDistinctTribes();
  List<PeerBaseData> findByWave(String wave, Pageable page);
  List<PeerBaseData> findByTribeId(int tribeId, Pageable page);
  List<PeerBaseData> findByWaveAndTribeId(String wave, int tribeId, Pageable page);
}
