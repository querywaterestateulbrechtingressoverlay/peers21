package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("api_peer_data")
public record ApiPeerData(
    @Id
    @Column("id")
    int id,
    @Column("login")
    String login,
    @Column("class_name") String className,
    @Column("parallel_name") String parallelName,
    @Column("exp_value") String expValue,
    @Column("exp_to_next_level") String expToNextLevel,
    @Column("campus_id") String campusId,
    PeerState state) {}