package com.example.demo.scraper.dto;

import com.example.demo.data.ApiPeerData;
import com.example.demo.data.PeerState;

public record ParticipantDTO(
    String login,
    String className,
    String parallelName,
    String expValue,
    String expToNextLevel,
    ParticipantCampusDTO campus,
    PeerState status) {
    public ApiPeerData toTableForm() {
        return new ApiPeerData(null, login, className, parallelName, expValue, expToNextLevel, campus.id(), status);
    }
    public ApiPeerData toTableForm(int id) {
        return new ApiPeerData(id, login, className, parallelName, expValue, expToNextLevel, campus.id(), status);
    }
}