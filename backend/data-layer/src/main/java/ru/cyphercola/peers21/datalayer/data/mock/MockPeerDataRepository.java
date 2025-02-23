package ru.cyphercola.peers21.datalayer.data.mock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MockPeerDataRepository extends CrudRepository<MockPeerData, Integer>, PagingAndSortingRepository<MockPeerData, Integer> {
  @Query("SELECT DISTINCT wave FROM mock_peer_data")
  List<String> findDistinctWaves();

  Page<MockPeerData> findByWave(String wave, Pageable page);

  Page<MockPeerData> findByTribeId(int tribeId, Pageable page);
  Page<MockPeerData> findByTribeIdAndWave(int tribeId, String wave, Pageable page);
}
