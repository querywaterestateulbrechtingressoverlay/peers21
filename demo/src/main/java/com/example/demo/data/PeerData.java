package com.example.demo.data;

import com.example.demo.scraper.dto.ParticipantDTO;
import com.example.demo.scraper.dto.ParticipantPointsDTO;
import com.example.demo.scraper.dto.ParticipantXpHistoryItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("peer_data")
public record PeerData(
    @Id @Column("id") Integer id,
    @Column("login") String login,
    @Column("intensive") int intensive,
    @Column("exp_value") String expValue,
    @Column("state") PeerState state,
    @Column("peer_review_points") int peerReviewPoints,
    @Column("code_review_points") int codeReviewPoints,
    @Column("coins") int coins) {
  public static PeerData updateFromDTO(PeerData prevValues, ParticipantDTO participantDTO, ParticipantPointsDTO pointsDTO) {
    return new PeerData(prevValues.id(), participantDTO.login(),
        prevValues.intensive(), participantDTO.expValue(),
        participantDTO.status(), pointsDTO.peerReviewPoints(),
        pointsDTO.codeReviewPoints(), pointsDTO.coins());
  }
  public static PeerData createFromDTO(ParticipantDTO participantDTO, int intensive) {
    return new PeerData(null, participantDTO.login(),
        intensive, participantDTO.expValue(),
        participantDTO.status(), 0,
        0, 0);
  }
}