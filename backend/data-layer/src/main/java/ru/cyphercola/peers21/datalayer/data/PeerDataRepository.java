package ru.cyphercola.peers21.datalayer.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface PeerDataRepository extends CrudRepository<PeerData, Integer>, PagingAndSortingRepository<PeerData, Integer> {
  Optional<PeerData> findFirst1ByLogin(String login);
  @Query("SELECT DISTINCT wave FROM peer_data")
  List<String> findDistinctWaves();

  Page<PeerData> findByWave(String wave, Pageable page);

  Page<PeerData> findByTribeId(int tribeId, Pageable page);
  Page<PeerData> findByTribeIdAndWave(int tribeId, String wave, Pageable page);
}
