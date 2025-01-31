package com.example.demo.data;

import com.example.demo.scraper.dto.ParticipantDTO;
import com.example.demo.scraper.dto.ParticipantPointsDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("peer_mutable_data")
public record PeerMutableData(
    @Id
    @Column("id")
    Integer id,
    @Column("peer_id")
    int peerId,
    @Column("state")
    PeerState state,
    @Column("tribe_points")
    int tribePoints,
    @Column("exp_value")
    int expValue,
    @Column("peer_review_points")
    int peerReviewPoints,
    @Column("code_review_points")
    int codeReviewPoints,
    @Column("coins")
    int coins
) {
    static public PeerMutableData updateFromDTO(Integer id, int peer_id, int tribePoints, ParticipantDTO participantDTO, ParticipantPointsDTO participantPointsDTO) {
        return new PeerMutableData(id, peer_id, participantDTO.status(), tribePoints, participantDTO.expValue(), participantPointsDTO.peerReviewPoints(), participantPointsDTO.codeReviewPoints(), participantPointsDTO.coins());
    }
}
