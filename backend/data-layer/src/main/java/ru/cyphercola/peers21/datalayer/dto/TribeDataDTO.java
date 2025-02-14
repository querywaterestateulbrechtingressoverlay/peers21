package ru.cyphercola.peers21.datalayer.dto;

import ru.cyphercola.peers21.datalayer.data.TribeData;

public record TribeDataDTO(
  Integer id,
  String name
) {
  public TribeData toEntity(Integer entityId) {
    return new TribeData(entityId, this.id, name);
  }
}
