package ru.cyphercola.peers21.datalayer;

import org.junit.jupiter.api.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import ru.cyphercola.peers21.datalayer.data.*;
import ru.cyphercola.peers21.datalayer.dto.PeerDataDTO;
import ru.cyphercola.peers21.datalayer.dto.PeerDataDTOList;
import ru.cyphercola.peers21.datalayer.dto.TribeDataDTO;
import ru.cyphercola.peers21.datalayer.dto.TribeDataDTOList;

import java.util.ArrayList;
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
    ResponseEntity<TribeDataDTOList> getTribeDataRE = this.template
      .withBasicAuth("user", "userpassword")
      .exchange("http://localhost:" + port + "/api/tribes", HttpMethod.GET, null, new ParameterizedTypeReference<TribeDataDTOList>() {});
    assertThat(getTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getTribeDataRE.getBody().tribes()).hasSize(2);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 123 && td.name().equals("amogi")).count()).isEqualTo(1);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 321 && td.name().equals("abobi")).count()).isEqualTo(1);
  }
  @Test
  @Order(4)
  void putTribesUnauthenticatedUser() {
    HttpEntity<TribeDataDTOList> tribeDataToPut = new HttpEntity<>(new TribeDataDTOList(List.of(new TribeDataDTO(222, "asd"))));
    ResponseEntity<Void> putTribeDataRE = this.template
      .withBasicAuth("user", "userpassword")
      .exchange("http://localhost:" + port + "/api/tribes", HttpMethod.PUT, tribeDataToPut, Void.class);
    assertThat(putTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
  @Test
  @Order(5)
  void putTribesAuthenticatedUser() {
    HttpEntity<TribeDataDTOList> tribeDataToPut = new HttpEntity<>(new TribeDataDTOList(List.of(new TribeDataDTO(222, "asd"))));
    ResponseEntity<Void> putTribeDataRE = this.template
      .withBasicAuth("admin", "adminpassword")
      .exchange("http://localhost:" + port + "/api/tribes", HttpMethod.PUT, tribeDataToPut, Void.class);
    assertThat(putTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
  @Test
  @Order(6)
  void getTribesAfterPut() {
    ResponseEntity<TribeDataDTOList> getTribeDataRE = this.template
      .withBasicAuth("user", "userpassword")
      .exchange("http://localhost:" + port + "/api/tribes", HttpMethod.GET, null, new ParameterizedTypeReference<TribeDataDTOList>() {});
    assertThat(getTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getTribeDataRE.getBody().tribes()).hasSize(3);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 123 && td.name().equals("amogi")).count()).isEqualTo(1);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 321 && td.name().equals("abobi")).count()).isEqualTo(1);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 222 && td.name().equals("asd")).count()).isEqualTo(1);
  }
  @Test
  @Order(7)
  void getPeersWithoutAuth() {
    ResponseEntity<String> peerDTOListEntity = this.template
      .exchange("http://localhost:" + port + "/api/peers", HttpMethod.GET, null, String.class);
    assertThat(peerDTOListEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
  @Test
  @Order(8)
  void getPeersWithoutParam() {
    ResponseEntity<PeerDataDTOList> peerDTOListEntity = this.template
      .withBasicAuth("user", "userpassword").exchange(
        "http://localhost:" + port + "/api/peers",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<PeerDataDTOList>() {});
    assertThat(peerDTOListEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(peerDTOListEntity.getBody().peers().size()).isEqualTo(6);
  }
  @Test
  @Order(9)
  void getPeersFilteredByTribeId() {
    ResponseEntity<PeerDataDTOList> response = template
      .withBasicAuth("user", "userpassword").exchange(
      "http://localhost:" + port + "/api/peers?tribeId=123",
      HttpMethod.GET,
      null,
      new ParameterizedTypeReference<PeerDataDTOList>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peers()).hasSize(3);
    assertThat(response.getBody().peers()).allMatch(peer -> peer.tribeId() == 123);
  }
  @Test
  @Order(10)
  void getPeersFilteredByWave() {
    ResponseEntity<PeerDataDTOList> response = template
      .withBasicAuth("user", "userpassword").exchange(
      "http://localhost:" + port + "/api/peers?wave=wave_1",
      HttpMethod.GET,
      null,
      new ParameterizedTypeReference<PeerDataDTOList>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peers()).hasSize(2);
    assertThat(response.getBody().peers()).allMatch(peer -> peer.wave().equals("wave_1"));
  }
  @Test
  @Order(11)
  void getPeersFilteredByTribeIdAndWave() {
    ResponseEntity<PeerDataDTOList> response = template
      .withBasicAuth("user", "userpassword").exchange(
      "http://localhost:" + port + "/api/peers?tribeId=321&wave=wave_2",
      HttpMethod.GET,
      null,
      new ParameterizedTypeReference<PeerDataDTOList>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peers()).hasSize(1);
    assertThat(response.getBody().peers().get(0).login()).isEqualTo("abobus2");
  }
  @Test
  @Order(12)
  void getPeersPaginated() {
    ResponseEntity<PeerDataDTOList> response = template
      .withBasicAuth("user", "userpassword").exchange(
      "http://localhost:" + port + "/api/peers?peersPerPage=2&page=1",
      HttpMethod.GET,
      null,
      new ParameterizedTypeReference<PeerDataDTOList>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peers()).hasSize(2);
    assertThat(response.getHeaders().containsKey(HttpHeaders.LINK)).isTrue();
    assertThat(response.getHeaders().get(HttpHeaders.LINK).stream().filter(s -> s.matches(".*page=0.*")).count()).isEqualTo(2);
    assertThat(response.getHeaders().get(HttpHeaders.LINK).stream().filter(s -> s.matches(".*page=2.*")).count()).isEqualTo(2);
  }
  @Test
  @Order(13)
  void getPeersSorted() {
    ResponseEntity<PeerDataDTOList> response = template
      .withBasicAuth("user", "userpassword").exchange(
      "http://localhost:" + port + "/api/peers?orderBy=login&orderAscending=false",
      HttpMethod.GET,
      null,
      new ParameterizedTypeReference<PeerDataDTOList>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peers()).hasSize(6);
    assertThat(response.getBody().peers().get(0).login()).isEqualTo("amogus3");
    assertThat(response.getBody().peers().get(5).login()).isEqualTo("abobus1");
  }
  @Test
  @Order(14)
  void putPeersUnauthenticatedUser() {
    HttpEntity<PeerDataDTOList> peerDataToPut = new HttpEntity<>(new PeerDataDTOList(List.of(
      new PeerDataDTO("gaben", "wave_1", 123, "ACTIVE", 19621103, 0, 1, 2, 4)
    )));
    ResponseEntity<Void> putPeerDataRE = this.template
      .withBasicAuth("user", "userpassword")
      .exchange("http://localhost:" + port + "/api/peers", HttpMethod.PUT, peerDataToPut, Void.class);
    assertThat(putPeerDataRE.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
  @Test
  @Order(15)
  void putPeersErroneousData() {
    HttpEntity<TribeDataDTOList> tribeDataToPut = new HttpEntity<>(new TribeDataDTOList(List.of(new TribeDataDTO(222, "asd"))));
    ResponseEntity<Void> putTribeDataRE = this.template
      .withBasicAuth("admin", "adminpassword")
      .exchange("http://localhost:" + port + "/api/peers", HttpMethod.PUT, tribeDataToPut, Void.class);
    assertThat(putTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
  @Test
  @Order(16)
  void putPeersAuthenticatedUser() {
    HttpEntity<PeerDataDTOList> peerDataToPut = new HttpEntity<>(new PeerDataDTOList(List.of(
      new PeerDataDTO("gaben", "wave_1", 123, "ACTIVE", 19621103, 0, 1, 2, 4)
    )));
    ResponseEntity<Void> putPeerDataRE = this.template
      .withBasicAuth("admin", "adminpassword")
      .exchange("http://localhost:" + port + "/api/peers", HttpMethod.PUT, peerDataToPut, Void.class);
    assertThat(putPeerDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
  @Test
  @Order(17)
  void getPeersAfterPut() {
    ResponseEntity<PeerDataDTOList> peerDTOListEntity = this.template
      .withBasicAuth("user", "userpassword").exchange(
        "http://localhost:" + port + "/api/peers",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<PeerDataDTOList>() {});
    assertThat(peerDTOListEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(peerDTOListEntity.getBody().peers().size()).isEqualTo(7);
  }
}
