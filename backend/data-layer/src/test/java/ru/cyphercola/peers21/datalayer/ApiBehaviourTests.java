package ru.cyphercola.peers21.datalayer;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.cyphercola.peers21.datalayer.DataLayerApplication;
import ru.cyphercola.peers21.datalayer.ApiController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.TestPropertySource;

import org.springframework.boot.web.client.RestTemplateBuilder;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = {"test_data.sql", "test_schema.sql"})
@TestPropertySource(locations="classpath:test.properties")
public class ApiBehaviourTests {
  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate template;
//  @TestBean
//  DataSource testDataSource;
//  static DataSource testDataSource() {
//    return new EmbeddedDatabaseBuilder()
//        .generateUniqueName(true)
//        .setType(EmbeddedDatabaseType.H2)
////        .addDefaultScripts()
//        .addScript("test_schema.sql")
//        .addScript("test_data.sql")
//        .build();
//  }

  @Test
  void testPing() throws Exception {
//    try (java.sql.Connection c = testDataSource.getConnection("sa","")) {
//      System.out.println(c.createStatement().executeQuery("SELECT * FROM api_users LIMIT 1;").getString(1));
//    }
    String maybePong = this.template.withBasicAuth("user", "userpassword").getForObject("http://localhost:" + port + "/api/ping", String.class);
    System.out.println(maybePong);
    assertThat(maybePong).isEqualTo("pong");
  }
}
