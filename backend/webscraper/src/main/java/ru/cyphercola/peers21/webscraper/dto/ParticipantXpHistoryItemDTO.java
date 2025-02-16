package ru.cyphercola.peers21.webscraper.dto;

import java.util.Date;

public record ParticipantXpHistoryItemDTO(
  int expValue,
  Date accrualDateTime) {}
