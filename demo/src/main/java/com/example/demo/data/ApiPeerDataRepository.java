package com.example.demo.data;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiPeerDataRepository extends CrudRepository<ApiPeerData, Integer> {
  @Query("SELECT login FROM api_peer_data")
  List<String> getLogins();

  ApiPeerData findByName(String peerUsername);

  List<ApiPeerData> findByWave(int wave);
}
