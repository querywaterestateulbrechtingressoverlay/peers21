package ru.cyphercola.peers21.backend.dto;

import java.util.List;

public record PeerDataPaginatedDTO(
    List<PeerDataDTO> peerData,
    int currentPage,
    int totalPages) {
}
