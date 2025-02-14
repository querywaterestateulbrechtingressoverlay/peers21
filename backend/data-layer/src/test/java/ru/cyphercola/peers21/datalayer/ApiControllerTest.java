package ru.cyphercola.peers21.datalayer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {ApiControllerTest.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ApiControllerTest {
  @Bean
  DataSource testDataSource() {
    return new EmbeddedDatabaseBuilder()
      .generateUniqueName(true)
      .setType(EmbeddedDatabaseType.H2)
      .addScripts("schema.sql", "test-data.sql")
      .build();
  }

  @Test
  void ping() {
  }

  @Test
  void getTribes() {
  }

  @Test
  void deleteTribe() {
  }

  @Test
  void insertOrUpdateTribes() {
  }

  @Test
  void getWaves() {
  }

  @Test
  void getPeers() {
  }

  @Test
  void deletePeer() {
  }

  @Test
  void putPeers() {
  }
}