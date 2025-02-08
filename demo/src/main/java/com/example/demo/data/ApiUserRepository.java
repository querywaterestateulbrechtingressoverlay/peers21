package com.example.demo.data;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ApiUserRepository extends CrudRepository<ApiUser, Integer> {
  Optional<ApiUser> findFirst1ByLogin(String login);
}
