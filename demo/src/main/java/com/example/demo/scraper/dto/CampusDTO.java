package com.example.demo.scraper.dto;

import com.example.demo.data.ApiCampusData;

import java.util.List;

public record CampusDTO(List<ApiCampusData> campuses) {
}
