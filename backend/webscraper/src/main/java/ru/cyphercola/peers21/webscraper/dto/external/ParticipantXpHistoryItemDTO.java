package ru.cyphercola.peers21.webscraper.dto.external;

import java.util.Date;

public record ParticipantXpHistoryItemDTO(
  int expValue,
  Date accrualDateTime) {}
