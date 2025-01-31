package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("peer_base_data")
public record PeerBaseData(
    @Id
    @Column("id")
    Integer id,
    @Column("login")
    String login,
    @Column("wave")
    String wave,
    @Column("intensive")
    int intensive,
    @Column("tribe_id")
    int tribeId,
    @MappedCollection(idColumn = "id")
    PeerMutableData peerMutableData
) { }
