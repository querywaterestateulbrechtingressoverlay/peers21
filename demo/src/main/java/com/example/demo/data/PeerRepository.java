package com.example.demo.data;


import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PeerRepository extends CrudRepository<Peer, Integer> {
    Peer findByName(String name);
    List<Peer> findByWave(int wave);
    @Query("SELECT * FROM peer_data")
    List<Peer> getAllPeers();
}
