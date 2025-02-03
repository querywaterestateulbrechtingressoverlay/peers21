package com.example.demo.data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PeerDataRepository extends CrudRepository<PeerData, Integer>, PagingAndSortingRepository<PeerData, Integer> {
  @Query("SELECT login FROM peer_data")
  List<String> getAllLogins();
  @Query("SELECT DISTINCT wave FROM peer_data")
  List<String> findDistinctWaves();
  @Query("SELECT DISTINCT tribe_data.tribe_id, tribe_data.name FROM peer_data LEFT JOIN tribe_data ON peer_data.tribe_id = tribe_data.tribe_id")
  List<TribeData> findDistinctTribes();
  List<PeerData> findByWave(String wave, Pageable page);
  List<PeerData> findByTribeId(int tribeId, Pageable page);
  List<PeerData> findByWaveAndTribeId(String wave, int tribeId, Pageable page);
}
