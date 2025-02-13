package ru.cyphercola.peers21.backend.scraper.dto;

import java.util.List;

public record ParticipantXpHistoryDTO(
  List<ParticipantXpHistoryItemDTO> expHistory
) {}
