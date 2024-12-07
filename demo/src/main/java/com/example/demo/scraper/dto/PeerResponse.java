package com.example.demo.scraper.dto;

import com.example.demo.scraper.ApiScraperService;

public record PeerResponse(String login, String className, String parallelName, int expValue, int level, int expToNextLevel, Campus campus, String status) {
    record Campus(String id, String shortName) {

    }
}
