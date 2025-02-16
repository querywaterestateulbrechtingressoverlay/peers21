package ru.cyphercola.peers21.webscraper.dto;

public record ParticipantDTO(
    String login,
    String className,
    String parallelName,
    int expValue,
    int expToNextLevel,
    ParticipantCampusDTO campus,
    String status) {
}