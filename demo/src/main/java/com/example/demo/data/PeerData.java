package com.example.demo.data;

import com.example.demo.scraper.dto.ParticipantDTO;
import com.example.demo.scraper.dto.ParticipantPointsDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("peer_data")
public record PeerData(
    @Id @Column("id") Integer id,
    @Column("login") String login,
    @Column("class_name") String className,
    @Column("parallel_name") String parallelName,
    @Column("exp_value") String expValue,
    @Column("exp_to_next_level") String expToNextLevel,
    @Column("campus_id") String campusId,
    @Column("state") PeerState state,
    @Column("peer_review_points") int peerReviewPoints,
    @Column("code_review_points") int codeReviewPoints,
    @Column("coins") int coins) {
  public static PeerData updateFromDTO(Integer id, ParticipantDTO participantDTO, ParticipantPointsDTO pointsDTO) {
    return new PeerData(id, participantDTO.login(), participantDTO.className(),
        participantDTO.parallelName(), participantDTO.expValue(),
        participantDTO.expToNextLevel(), participantDTO.campus().id(),
        participantDTO.status(), pointsDTO.peerReviewPoints(),
        pointsDTO.codeReviewPoints(), pointsDTO.coins());
  }
  public static PeerData createFromDTO(ParticipantDTO participantDTO) {
    return new PeerData(null, participantDTO.login(), participantDTO.className(),
        participantDTO.parallelName(), participantDTO.expValue(),
        participantDTO.expToNextLevel(), participantDTO.campus().id(),
        participantDTO.status(), 0,
        0, 0);
  }
}