package ru.cyphercola.peers21.datalayer.data.mock;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.cyphercola.peers21.datalayer.dto.TribeDataDTO;

@Table("mock_tribe_data")
public record MockTribeData(
  @Id
  @Column("id")
  Integer id,
  @Column("tribe_id")
  int tribeId,
  @Column("name")
  String name
) {
  public TribeDataDTO toDTO() {
    return new TribeDataDTO(tribeId, name);
  }
}