package ru.cyphercola.peers21.datalayer;

import org.apache.coyote.Response;
import org.junit.jupiter.api.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.cyphercola.peers21.datalayer.DataLayerApplication;
import ru.cyphercola.peers21.datalayer.ApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.TestPropertySource;

import org.springframework.boot.web.client.RestTemplateBuilder;
import ru.cyphercola.peers21.datalayer.data.*;
import ru.cyphercola.peers21.datalayer.dto.TribeDataDTO;

import javax.sql.DataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiBehaviourTests {
  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate template;
  @Autowired
  private ApiUserRepository apiUserRepository;
  @Test
  @Order(1)
  void pingUnauthorized() {
    ResponseEntity<String> pongEntity = this.template
      .getForEntity("http://localhost:" + port + "/api/ping", String.class);
    assertThat(pongEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
  @Test
  @Order(2)
  void pingAuthorized() {
    String maybePong = this.template
      .withBasicAuth("user", "userpassword")
      .getForObject("http://localhost:" + port + "/api/ping", String.class);
    assertThat(maybePong).isEqualTo("pong");
  }
  @Test
  @Order(3)
  void getTribes() {
    ResponseEntity<List<TribeData>> getTribeDataRE = this.template
      .withBasicAuth("user", "userpassword")
      .exchange("http://localhost:" + port + "/api/tribes", HttpMethod.GET, null, new ParameterizedTypeReference<List<TribeData>>() {});
    assertThat(getTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getTribeDataRE.getBody().size()).isEqualTo(2);
    assertThat(getTribeDataRE.getBody().stream().filter((td) -> td.tribeId() == 123 && td.name().equals("amogi")).count()).isEqualTo(1);
    assertThat(getTribeDataRE.getBody().stream().filter((td) -> td.tribeId() == 321 && td.name().equals("abobi")).count()).isEqualTo(1);
  }
  @Test
  @Order(4)
  void putTribesUnauthorizedUser() {
    HttpEntity<List<TribeDataDTO>> tribeDataToPut = new HttpEntity<>(List.of(new TribeDataDTO(222, "asd")));
    ResponseEntity<Void> putTribeDataRE = this.template
      .withBasicAuth("user", "userpassword")
      .exchange("http://localhost:" + port + "/api/tribes", HttpMethod.PUT, tribeDataToPut, Void.class);
    assertThat(putTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
  @Test
  @Order(5)
  void putTribesAuthorizedUser() {
    HttpEntity<List<TribeDataDTO>> tribeDataToPut = new HttpEntity<>(List.of(new TribeDataDTO(222, "asd")));
    ResponseEntity<Void> putTribeDataRE = this.template
      .withBasicAuth("admin", "adminpassword")
      .exchange("http://localhost:" + port + "/api/tribes", HttpMethod.PUT, tribeDataToPut, Void.class);
    assertThat(putTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
