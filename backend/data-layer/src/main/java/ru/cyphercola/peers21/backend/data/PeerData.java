package ru.cyphercola.peers21.backend.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.cyphercola.peers21.backend.dto.PeerDataDTO;

@Table("peer_data")
public record PeerData(
    @Id
    @Column("id")
    Integer id,
    @Column("login")
    String login,
    @Column("wave")
    String wave,
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
  public PeerDataDTO toDTO() {
    return new PeerDataDTO(login, wave, tribeId, state.toString(), tribePoints, expValue, peerReviewPoints, codeReviewPoints, coins);
  }
}
