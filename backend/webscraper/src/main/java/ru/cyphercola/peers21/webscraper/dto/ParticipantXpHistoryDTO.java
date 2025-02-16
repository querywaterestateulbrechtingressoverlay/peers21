package ru.cyphercola.peers21.webscraper.dto;

import java.util.List;

public record ParticipantXpHistoryDTO(
  List<ParticipantXpHistoryItemDTO> expHistory
) {}
