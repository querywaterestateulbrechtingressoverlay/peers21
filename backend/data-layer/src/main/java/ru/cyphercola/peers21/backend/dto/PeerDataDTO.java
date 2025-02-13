package ru.cyphercola.peers21.backend.dto;

import ru.cyphercola.peers21.backend.data.PeerData;
import ru.cyphercola.peers21.backend.data.PeerState;

public record PeerDataDTO(
  String login,
  String wave,
  Integer tribeId,
  String state,
  Integer tribePoints,
  Integer expValue,
  Integer peerReviewPoints,
  Integer codeReviewPoints,
  Integer coins
) {
  public PeerData toEntity(Integer id) {
    return new PeerData(id, login, wave, tribeId, PeerState.fromString(state), tribePoints, expValue, peerReviewPoints, codeReviewPoints, coins);
  }
}
