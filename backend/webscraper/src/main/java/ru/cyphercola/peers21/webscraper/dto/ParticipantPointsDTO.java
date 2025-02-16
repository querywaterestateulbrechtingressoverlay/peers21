package ru.cyphercola.peers21.webscraper.dto;

public record ParticipantPointsDTO(
  int peerReviewPoints,
  int codeReviewPoints,
  int coins) {
}
