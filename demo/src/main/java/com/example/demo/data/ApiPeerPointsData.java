package com.example.demo.data;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("api_peer_points_data")
public record ApiPeerPointsData(
    @Column("peer_review_points")
    int peerReviewPoints,
    @Column("code_review_points")
    int codeReviewPoints,
    @Column("coins")
    int coins) {}
