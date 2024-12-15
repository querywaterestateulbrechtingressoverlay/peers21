package com.example.demo.data;

import org.springframework.data.relational.core.mapping.Column;

public record ApiPeerData(
    String login,
    @Column("class_name") String className,
    @Column("parallel_name") String parallelName,
    @Column("exp_value") String expValue,
    @Column("exp_to_next_level") String expToNextLevel,
    @Column("campus_id") String campusId,
    PeerState state) {}