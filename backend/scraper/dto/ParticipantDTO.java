package ru.cyphercola.peers21.backend.scraper.dto;

import ru.cyphercola.peers21.backend.data.PeerState;

public record ParticipantDTO(
    String login,
    String className,
    String parallelName,
    int expValue,
    int expToNextLevel,
    ParticipantCampusDTO campus,
    PeerState status) {
}