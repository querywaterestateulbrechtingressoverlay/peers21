package com.example.demo.data;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PeerDataRepository extends CrudRepository<PeerData, Integer> {
  @Query("SELECT login FROM peer_data")
  List<String> getLogins();

  PeerData findByLogin(String peerUsername);

//  List<ApiPeerData> findByWave(int wave);
}
