package ru.cyphercola.peers21.datalayer.dto;

import ru.cyphercola.peers21.datalayer.data.PeerData;
import ru.cyphercola.peers21.datalayer.data.PeerState;

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
  public PeerData toEntity(Integer entityId) {
    return new PeerData(entityId, login, wave, tribeId, PeerState.fromString(state), tribePoints, expValue, peerReviewPoints, codeReviewPoints, coins);
  }
}
