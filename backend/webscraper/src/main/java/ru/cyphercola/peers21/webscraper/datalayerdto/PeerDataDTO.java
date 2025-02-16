package ru.cyphercola.peers21.webscraper.datalayerdto;

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
}
