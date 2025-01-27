package com.example.demo.scraper.dto;

import com.example.demo.data.PeerState;

public record ParticipantDTO(
    String login,
    String className,
    String parallelName,
    int expValue,
    int expToNextLevel,
    ParticipantCampusDTO campus,
    PeerState status) {
}