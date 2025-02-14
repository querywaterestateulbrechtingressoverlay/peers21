package ru.cyphercola.peers21.datalayer.data;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TribeDataRepository extends CrudRepository<TribeData, Integer> {
  Optional<TribeData> findFirst1ByTribeId(Integer tribeId);
  Integer findByName(String name);
}
