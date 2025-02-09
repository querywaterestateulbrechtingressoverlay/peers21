package ru.cyphercola.peers21.backend.dto;

import ru.cyphercola.peers21.backend.data.PeerData;

import java.util.List;

public record PeerDataDTO(
  List<PeerData> peerData,
  int currentPage,
  int totalPages
) {
}
