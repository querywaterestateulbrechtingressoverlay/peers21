package com.example.demo.scraper.dto;

import com.example.demo.scraper.ApiScraperService;

public class PeerResponse {
    static class Campus {
        String id;
        String shortName;

        public Campus(String id, String shortName) {
            this.id = id;
            this.shortName = shortName;
        }
    }

    String login;
    String className;
    String parallelName;
    int expValue;
    int level;
    int expToNextLevel;
    Campus campus;
    String status;

    public PeerResponse(String login, String className, String parallelName, int expValue, int level, int expToNextLevel, Campus campus, String status) {
        this.login = login;
        this.className = className;
        this.parallelName = parallelName;
        this.expValue = expValue;
        this.level = level;
        this.expToNextLevel = expToNextLevel;
        this.campus = campus;
        this.status = status;
    }
}
