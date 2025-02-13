package ru.cyphercola.peers21.backend.scraper.dto;

import java.util.Date;

public record ParticipantXpHistoryItemDTO(
  int expValue,
  Date accrualDateTime) {}
