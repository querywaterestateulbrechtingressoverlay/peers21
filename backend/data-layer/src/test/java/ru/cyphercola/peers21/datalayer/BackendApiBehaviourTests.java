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

import org.springframework.util.MultiValueMap;
import ru.cyphercola.peers21.datalayer.data.*;
import ru.cyphercola.peers21.datalayer.dto.*;

import javax.swing.plaf.multi.MultiLabelUI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackendApiBehaviourTests {
  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate template;
  @Autowired
  private ApiUserRepository apiUserRepository;
  MultiValueMap<String, String> apiAuthorized() {
    String authorizedToken = this.template
      .withBasicAuth("test_user_1", "password")
      .postForEntity("http://localhost:" + port + "/api/auth/login", null, JWTokenDTO.class).getBody().token();
    return MultiValueMap.fromSingleValue(Map.of("Authorization", "Bearer " + authorizedToken));

  }
  MultiValueMap<String, String> apiUnauthorized() {
    String unauthorizedToken = this.template
      .withBasicAuth("test_user_2", "password")
      .postForEntity("http://localhost:" + port + "/api/auth/login", null, JWTokenDTO.class).getBody().token();
    return MultiValueMap.fromSingleValue(Map.of("Authorization", "Bearer " + unauthorizedToken));
  }
  @Test
  @Order(1)
  void ping() {
    ResponseEntity<String> pongEntity = this.template
      .getForEntity("http://localhost:" + port + "/api/ping", String.class);
    assertThat(pongEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
  @Test
  @Order(2)
  void loginIncorrectCredentials() {
    ResponseEntity<String> response = this.template
      .postForEntity("http://localhost:" + port + "/api/auth/login", null, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
  @Test
  @Order(3)
  void loginCorrectCredentials() {
    ResponseEntity<JWTokenDTO> response = this.template
      .withBasicAuth("test_user_1", "password")
      .postForEntity("http://localhost:" + port + "/api/auth/login", null, JWTokenDTO.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().token()).isNotNull();
  }
  @Test
  @Order(4)
  void getTribesUnauthenticated() {
    ResponseEntity<TribeDataDTOList> getTribeDataRE = this.template
      .exchange("http://localhost:" + port + "/api/backend/tribes", HttpMethod.GET, null, new ParameterizedTypeReference<TribeDataDTOList>() {});
    assertThat(getTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
  @Test
  @Order(5)
  void getTribesAuthorized() {
    ResponseEntity<TribeDataDTOList> getTribeDataRE = this.template
      .exchange("http://localhost:" + port + "/api/backend/tribes", HttpMethod.GET, new HttpEntity<>(apiAuthorized()), new ParameterizedTypeReference<TribeDataDTOList>() {});
    assertThat(getTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getTribeDataRE.getBody().tribes()).hasSize(2);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 123 && td.name().equals("amogi")).count()).isEqualTo(1);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 321 && td.name().equals("abobi")).count()).isEqualTo(1);
  }
  @Test
  @Order(6)
  void putTribesUnauthorized() {
    TribeDataDTOList tribeDataToPut = new TribeDataDTOList(List.of(new TribeDataDTO(222, "asd")));
    ResponseEntity<Void> putTribeDataRE = this.template
      .exchange("http://localhost:" + port + "/api/backend/tribes", HttpMethod.PUT, new HttpEntity<>(tribeDataToPut, apiUnauthorized()), Void.class);
    assertThat(putTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
  @Test
  @Order(7)
  void putTribesAuthorized() {
    TribeDataDTOList tribeDataToPut = new TribeDataDTOList(List.of(new TribeDataDTO(222, "asd")));
    ResponseEntity<Void> putTribeDataRE = this.template
      .exchange("http://localhost:" + port + "/api/backend/tribes", HttpMethod.PUT, new HttpEntity<>(tribeDataToPut, apiAuthorized()), Void.class);
    assertThat(putTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
  @Test
  @Order(8)
  void getTribesAfterPut() {
    ResponseEntity<TribeDataDTOList> getTribeDataRE = this.template
      .exchange("http://localhost:" + port + "/api/backend/tribes", HttpMethod.GET, new HttpEntity<>(apiAuthorized()), new ParameterizedTypeReference<TribeDataDTOList>() {});
    assertThat(getTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getTribeDataRE.getBody().tribes()).hasSize(3);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 123 && td.name().equals("amogi")).count()).isEqualTo(1);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 321 && td.name().equals("abobi")).count()).isEqualTo(1);
    assertThat(getTribeDataRE.getBody().tribes().stream().filter((td) -> td.id() == 222 && td.name().equals("asd")).count()).isEqualTo(1);
  }
  @Test
  @Order(9)
  void getPeersUnauthenticated() {
    ResponseEntity<String> peerDTOListEntity = this.template
      .exchange("http://localhost:" + port + "/api/backend/peers", HttpMethod.GET, null, String.class);
    assertThat(peerDTOListEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
  @Test
  @Order(10)
  void getPeersWithoutParam() {
    ResponseEntity<PeerDataPaginatedDTO> peerDTOListEntity = this.template
      .exchange(
        "http://localhost:" + port + "/api/backend/peers",
        HttpMethod.GET,
        new HttpEntity<>(apiAuthorized()),
        new ParameterizedTypeReference<PeerDataPaginatedDTO>() {});
    assertThat(peerDTOListEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(peerDTOListEntity.getBody().peerData().size()).isEqualTo(6);
  }
  @Test
  @Order(11)
  void getPeersFilteredByTribeId() {
    ResponseEntity<PeerDataPaginatedDTO> response = template
      .exchange(
      "http://localhost:" + port + "/api/backend/peers?tribeId=123",
      HttpMethod.GET,
        new HttpEntity<>(apiAuthorized()),
      new ParameterizedTypeReference<PeerDataPaginatedDTO>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peerData()).hasSize(3);
    assertThat(response.getBody().peerData()).allMatch(peer -> peer.tribeId() == 123);
  }
  @Test
  @Order(12)
  void getPeersFilteredByWave() {
    ResponseEntity<PeerDataPaginatedDTO> response = template.exchange(
      "http://localhost:" + port + "/api/backend/peers?wave=wave_1",
      HttpMethod.GET,
        new HttpEntity<>(apiAuthorized()),
      new ParameterizedTypeReference<PeerDataPaginatedDTO>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peerData()).hasSize(2);
    assertThat(response.getBody().peerData()).allMatch(peer -> peer.wave().equals("wave_1"));
  }
  @Test
  @Order(13)
  void getPeersFilteredByTribeIdAndWave() {
    ResponseEntity<PeerDataPaginatedDTO> response = template.exchange(
      "http://localhost:" + port + "/api/backend/peers?tribeId=321&wave=wave_2",
      HttpMethod.GET,
        new HttpEntity<>(apiAuthorized()),
      new ParameterizedTypeReference<PeerDataPaginatedDTO>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peerData()).hasSize(1);
    assertThat(response.getBody().peerData().get(0).login()).isEqualTo("abobus2");
  }
  @Test
  @Order(14)
  void getPeersPaginated() {
    ResponseEntity<PeerDataPaginatedDTO> response = template.exchange(
      "http://localhost:" + port + "/api/backend/peers?peersPerPage=2&page=1",
      HttpMethod.GET,
        new HttpEntity<>(apiAuthorized()),
      new ParameterizedTypeReference<PeerDataPaginatedDTO>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peerData()).hasSize(2);
    assertThat(response.getHeaders().containsKey(HttpHeaders.LINK)).isTrue();
    assertThat(response.getHeaders().get(HttpHeaders.LINK).stream().filter(s -> s.matches(".*page=0.*")).count()).isEqualTo(2);
    assertThat(response.getHeaders().get(HttpHeaders.LINK).stream().filter(s -> s.matches(".*page=2.*")).count()).isEqualTo(2);
  }
  @Test
  @Order(15)
  void getPeersSorted() {
    ResponseEntity<PeerDataPaginatedDTO> response = template.exchange(
      "http://localhost:" + port + "/api/backend/peers?orderBy=login&orderAscending=false",
      HttpMethod.GET,
        new HttpEntity<>(apiAuthorized()),
      new ParameterizedTypeReference<PeerDataPaginatedDTO>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().peerData()).hasSize(6);
    assertThat(response.getBody().peerData().get(0).login()).isEqualTo("amogus3");
    assertThat(response.getBody().peerData().get(5).login()).isEqualTo("abobus1");
  }
  @Test
  @Order(16)
  void putPeersUnauthenticated() {
    PeerDataDTOList peerDataToPut = new PeerDataDTOList(List.of(
      new PeerDataDTO("gaben", "wave_1", 123, "ACTIVE", 19621103, 0, 1, 2, 4)
    ));
    ResponseEntity<Void> putPeerDataRE = this.template
      .exchange("http://localhost:" + port + "/api/backend/peers", HttpMethod.PUT, new HttpEntity<>(peerDataToPut, apiUnauthorized()), Void.class);
    assertThat(putPeerDataRE.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
  @Test
  @Order(17)
  void putPeersErroneousData() {
    TribeDataDTOList tribeDataToPut = new TribeDataDTOList(List.of(new TribeDataDTO(222, "asd")));
    ResponseEntity<Void> putTribeDataRE = this.template
      .exchange("http://localhost:" + port + "/api/backend/peers", HttpMethod.PUT, new HttpEntity<>(tribeDataToPut, apiAuthorized()), Void.class);
    assertThat(putTribeDataRE.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
  @Test
  @Order(18)
  void putPeersAuthorized() {
    PeerDataDTOList peerDataToPut = new PeerDataDTOList(List.of(
      new PeerDataDTO("gaben", "wave_1", 123, "ACTIVE", 19621103, 0, 1, 2, 4)
    ));
    ResponseEntity<Void> putPeerDataRE = this.template
      .exchange("http://localhost:" + port + "/api/backend/peers", HttpMethod.PUT, new HttpEntity<>(peerDataToPut, apiAuthorized()), Void.class);
    assertThat(putPeerDataRE.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
  @Test
  @Order(19)
  void getPeersAfterPut() {
    ResponseEntity<PeerDataPaginatedDTO> peerDTOListEntity = this.template
      .exchange(
        "http://localhost:" + port + "/api/backend/peers",
        HttpMethod.GET,
        new HttpEntity<>(apiAuthorized()),
        new ParameterizedTypeReference<PeerDataPaginatedDTO>() {});
    assertThat(peerDTOListEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(peerDTOListEntity.getBody().peerData().size()).isEqualTo(7);
  }
}
