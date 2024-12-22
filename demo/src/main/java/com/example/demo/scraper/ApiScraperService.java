package com.example.demo.scraper;

import com.example.demo.data.*;
import com.example.demo.data.Peer;
import com.example.demo.scraper.dto.ApiKeyResponse;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

@EnableScheduling
@Service
public class ApiScraperService {
  private final String apiUrl = "https://edu-api.21-school.ru/services/21-school/api/v1";
  private final LinkedMultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
  private String apiKey = "";
  private final long lastUpdateDate = System.currentTimeMillis();
  private long keyExpiryDate = System.currentTimeMillis();
  Logger logger = LoggerFactory.getLogger(ApiScraperService.class);
  private RestClient apiReqClient = RestClient.builder()
    .baseUrl(apiUrl)
    .build();
  @Autowired
  ApiPeerDataRepository peerRepo;
  @Autowired
  ApiPeerPointsDataRepository peerPointsRepo;
  ApiScraperService() {
    boolean error = false;
    logger.info("retrieving API username from environment variables...");
    String apiUsername = System.getenv("API_USERNAME");
    if (apiUsername == null) {
      logger.error("System variable API_USERNAME is not set");
      error = true;
    }
    logger.info("retrieving API password from environment variables...");
    String apiPassword = System.getenv("API_PASSWORD");
    if (apiPassword == null) {
      logger.error("System variable API_PASSWORD is not set");
      error = true;
    }
    if (error) {
      throw new RuntimeException("an error happened during the retrieval of system variables");
    } else {
      logger.info("successfully obtained API username and password");
      tokenRequestBody.add("username", apiUsername);
      tokenRequestBody.add("password", apiPassword);
      tokenRequestBody.add("grant_type", "password");
      tokenRequestBody.add("client_id", "s21-open-api");
    }
  }

  public void updateApiKey() {
    logger.info("updating API key...");
    RestClient apiReqClient = RestClient.builder()
      .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
      .build();
    String tokenUrl = "https://auth.sberclass.ru/auth/realms/EduPowerKeycloak/protocol/openid-connect/token";
    ApiKeyResponse keyEntity = apiReqClient.post()
      .uri(tokenUrl)
      .body(tokenRequestBody)
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> logger.error("couldn't update API key, request = " + request + ", response = " + response)))
      .body(ApiKeyResponse.class);
    if (keyEntity != null) {
      apiKey = keyEntity.accessToken();
      logger.info("expires_in = " + keyEntity.expiresIn());
      keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn() * 1000L;
      logger.info("successfully updated API key, new key expiry date = " + LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiryDate), TimeZone.getDefault().toZoneId()));
      logger.info("key = " + apiKey);
      apiReqClient.mutate().defaultHeader("Authorization", "Bearer " + apiKey);
    } else {
      apiKey = "";
    }
  }

  ApiPeerData sendPeerInfoRequest(RestClient client, String peerName) {
    return client.get()
      .uri(apiUrl + "/participants/" + peerName)
      .retrieve()
      .body(ApiPeerData.class);
  }

  <T> T tryToRetrieveUntilSuccess(Class<T> clazz, String url, RestClient client) {
    AtomicBoolean tooManyRequests = new AtomicBoolean(false);
    T returnValue;
    while (true) {
      returnValue = client.get()
        .uri(url)
        .accept(MediaType.APPLICATION_JSON)
        .exchange((req, resp) -> {
          if (resp.getStatusCode() != HttpStatus.OK) {
            if (resp.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
              tooManyRequests.set(true);
              return null;
            } else {
              throw new RestClientResponseException(req.getMethod().toString() + req.getURI(), resp.getStatusCode(), resp.getStatusText(), req.getHeaders(), resp.getBody().readAllBytes(), Charset.defaultCharset());
            }
          } else {
            return resp.bodyTo(clazz);
          }
        });
      if (tooManyRequests.get()) {
        continue;
      }
      break;
    }
    return returnValue;
  }

  List<ApiCampusData> listCampuses() {
    logger.info("retrieving campus list...");
    if (System.currentTimeMillis() >= keyExpiryDate) {
      logger.info("API key is out of date, updating... (current timestamp is " + System.currentTimeMillis() + "), key expiry timestamp is " + keyExpiryDate);
      updateApiKey();
    }
  }

  @Scheduled(fixedRateString = "PT1M")
  public void updatePeerList() {
    logger.info("updating peer info...");
    if (System.currentTimeMillis() >= keyExpiryDate) {
      logger.info("API key is out of date, updating... (current timestamp is " + System.currentTimeMillis() + "), key expiry timestamp is " + keyExpiryDate);
      updateApiKey();
    }
    var peerList = StreamSupport.stream(peerRepo.findAll().spliterator(), true).toList();
    var peerPointsList = StreamSupport.stream(peerPointsRepo.findAll().spliterator(), true).toList();

    var changedPeerData = new ArrayList<ApiPeerData>();
    var changedPeerPointsData = new ArrayList<ApiPeerPointsData>();

    AtomicInteger counter = new AtomicInteger(0);
    Bucket bucket = Bucket.builder()
      .addLimit(b -> b.capacity(3).refillGreedy(2, Duration.ofSeconds(1))).build();
    var peerFutures = new ArrayList<CompletableFuture<Void>>();
    try (ExecutorService rateLimitedExecutor = Executors.newFixedThreadPool(3)) {
      for (ApiPeerData peer : peerList) {
        CompletableFuture<Void> asd = CompletableFuture.supplyAsync(() -> {
          logger.info("ASD");
          if (counter.get() == peerList.size()) {
            rateLimitedExecutor.shutdown();
          }
          logger.info("peer " + peer.login());
          try {
            logger.info("ASD");
            ApiPeerData peerResponse = null;
            ApiPeerPointsData peerPointsResponse = null;
            while (peerResponse == null || peerPointsResponse == null) {
              if (peerResponse == null && bucket.tryConsume(1)) {
                peerResponse = tryToRetrieveUntilSuccess(ApiPeerData.class, apiUrl + "/participants/" + peer.login(), apiReqClient);
              }
              if (peerPointsResponse == null && bucket.tryConsume(1)) {
                peerPointsResponse = tryToRetrieveUntilSuccess(ApiPeerPointsData.class, apiUrl + "/participants/" + peer.login() + "/points", apiReqClient);
              }
            }
            changedPeerData.add(peerResponse);
            changedPeerPointsData.add(peerPointsResponse);
          } catch (RestClientResponseException e) {
            logger.error("received error " + e.getStatusCode() + ", message = " + e.getResponseBodyAsString());
          } catch (Exception e) {
            logger.error(e.getMessage());
          }
          return null;
          }, rateLimitedExecutor);
        peerFutures.add(asd);
      }
      peerFutures.forEach(CompletableFuture::join);
    }
    if (!(changedPeerData.isEmpty() && changedPeerPointsData.isEmpty())) {
      peerRepo.saveAll(changedPeerData);
      peerPointsRepo.saveAll(changedPeerPointsData);
      logger.info("update finished");
    } else {
      logger.info("no peers were updated");
    }
  }
  public Date getLastUpdateDate() {
        return new Date(lastUpdateDate);
    }

}
