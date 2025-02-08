package ru.cyphercola.peers21.backend.data;

import org.springframework.data.repository.CrudRepository;

public interface TribeDataRepository extends CrudRepository<TribeData, Integer> {
  Integer findByName(String name);
}
