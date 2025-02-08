package ru.cyphercola.peers21.backend.scraper.dto;

public record ParticipantPointsDTO(
  int peerReviewPoints,
  int codeReviewPoints,
  int coins) {
}
