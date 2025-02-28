package ru.cyphercola.peers21.webscraper.dto.external;

public record ParticipantPointsDTO(
  int peerReviewPoints,
  int codeReviewPoints,
  int coins) {
}
