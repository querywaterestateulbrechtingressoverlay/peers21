package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

public record TribeParticipantData (
  @Id
  @Column("id") Integer id,
  @Column("tribe_id") int tribeId,
  @Column("peer_login") String peerLogin
) {}
