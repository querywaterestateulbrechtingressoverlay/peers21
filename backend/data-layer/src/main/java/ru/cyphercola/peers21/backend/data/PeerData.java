package ru.cyphercola.peers21.backend.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("peer_data")
public record PeerData(
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
//    public static PeerData createFromDTO(ParticipantDTO peerDTO, ParticipantPointsDTO ptsDTO, int intensive, int tribeId, int tribePoints) {
//        return new PeerData(
//          null, peerDTO.login(), peerDTO.className(),
//          intensive, tribeId, peerDTO.status(), tribePoints,
//          peerDTO.expValue(), ptsDTO.peerReviewPoints(),
//          ptsDTO.codeReviewPoints(), ptsDTO.coins()
//        );
//    }
//    public PeerData updateFromDTO(ParticipantDTO peerDTO, ParticipantPointsDTO ptsDTO, int tribePoints) {
//        return new PeerData(
//          id, login, peerDTO.className(), intensive, tribeId,
//          peerDTO.status(), tribePoints, peerDTO.expValue(),
//          ptsDTO.peerReviewPoints(), ptsDTO.codeReviewPoints(), ptsDTO.coins()
//        );
//    }
}
