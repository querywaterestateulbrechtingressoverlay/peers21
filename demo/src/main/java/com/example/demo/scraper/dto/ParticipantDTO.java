package com.example.demo.scraper.dto;

import com.example.demo.data.PeerData;
import com.example.demo.data.PeerState;

public record ParticipantDTO(
    String login,
    String className,
    String parallelName,
    String expValue,
    String expToNextLevel,
    ParticipantCampusDTO campus,
    PeerState status) {
}