package ru.cyphercola.peers21.datalayer.data.mock;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MockTribeDataRepository extends CrudRepository<MockTribeData, Integer> {
  Optional<MockTribeData> findFirst1ByTribeId(Integer tribeId);
  Integer findByName(String name);
}
