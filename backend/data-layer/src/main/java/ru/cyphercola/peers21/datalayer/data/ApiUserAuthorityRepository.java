package ru.cyphercola.peers21.datalayer.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApiUserAuthorityRepository extends CrudRepository<ApiUserAuthority, Integer> {
  List<ApiUserAuthority> findByApiUserLogin(String login);
}
