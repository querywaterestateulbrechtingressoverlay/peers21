package ru.cyphercola.peers21.webscraper.dto.internal;

import java.util.List;

public record PeerDataPaginatedDTO(
    List<PeerDataDTO> peerData,
    Integer currentPage,
    Integer totalPages,
    String firstPageUrl,
    String previousPageUrl,
    String nextPageUrl,
    String lastPageUrl) {
}
