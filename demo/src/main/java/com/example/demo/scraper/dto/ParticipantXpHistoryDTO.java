package com.example.demo.scraper.dto;

import java.util.List;

public record ParticipantXpHistoryDTO(
  List<ParticipantXpHistoryItemDTO> expHistory
) {}
