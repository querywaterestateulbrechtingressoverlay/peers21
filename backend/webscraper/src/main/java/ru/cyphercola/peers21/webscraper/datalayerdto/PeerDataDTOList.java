package ru.cyphercola.peers21.webscraper.datalayerdto;

import java.util.List;

public record PeerDataDTOList(
  List<PeerDataDTO> peers
) {
}
