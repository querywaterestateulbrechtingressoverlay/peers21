package com.example.demo.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("peer_data")
public record Peer(@Id String name, PeerState state, int wave, int intensive, int xp, @Column("peer_review_points") int peerReviewPoints, @Column("code_review_points") int codeReviewPoints, int coins) {}
