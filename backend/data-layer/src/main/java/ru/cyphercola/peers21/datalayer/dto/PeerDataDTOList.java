package ru.cyphercola.peers21.datalayer.dto;

import java.util.List;

public record PeerDataDTOList(
  List<PeerDataDTO> peers
) {
}
