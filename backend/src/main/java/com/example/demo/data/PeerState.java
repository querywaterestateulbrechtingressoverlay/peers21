package com.example.demo.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PeerState {
    ACTIVE, TEMPORARY_BLOCKING, EXPELLED, BLOCKED, FROZEN;
    @JsonCreator
    public static PeerState fromString(String value) {
        return PeerState.valueOf(value);
    }
}
