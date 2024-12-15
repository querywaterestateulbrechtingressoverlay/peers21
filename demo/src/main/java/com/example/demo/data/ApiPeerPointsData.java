package com.example.demo.data;

import org.springframework.data.relational.core.mapping.Column;

public record ApiPeerPointsData(
    @Column("peer_review_points") int peerReviewPoints,
    @Column("code_review_points") int codeReviewPoints,
    int coins) {}
