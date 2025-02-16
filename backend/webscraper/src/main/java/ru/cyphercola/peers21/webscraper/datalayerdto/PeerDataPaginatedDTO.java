package ru.cyphercola.peers21.webscraper.datalayerdto;

import java.util.List;

public record PeerDataPaginatedDTO(
    List<PeerDataDTO> peerData,
    int currentPage,
    int totalPages) {
}
