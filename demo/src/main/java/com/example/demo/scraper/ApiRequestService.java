package com.example.demo.scraper;

import com.example.demo.scraper.dto.ApiKeyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.concurrent.*;

@Service
@EnableConfigurationProperties(com.example.demo.scraper.ApiRequestServiceProperties.class)
public class ApiRequestService {
  private final String tokenEndpointUrl;
  private final String apiBaseUrl;
  private final Bucket reqBucket;
  private final Logger logger = LoggerFactory.getLogger(ApiRequestService.class);
  private final LinkedMultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
  private long keyExpiryDate = System.currentTimeMillis();
  private RestClient apiClient;

  private final ExecutorService requestExecutor;
  @Autowired
  private RestClient.Builder restClientBuilder;

  @Autowired
  ApiRequestService(ApiRequestServiceProperties properties, RestClient.Builder apiClientBuilder) {
    this.tokenEndpointUrl = properties.tokenEndpointUrl();
    boolean error = false;
    logger.info("retrieving API username from environment variables...");
    String apiUsername = System.getenv(properties.envUsernameVariable());
    if (apiUsername == null) {
      logger.error("System variable {} is not set", properties.envUsernameVariable());
      error = true;
    }
    logger.info("retrieving API password from environment variables...");
    String apiPassword = System.getenv(properties.envPasswordVariable());
    if (apiPassword == null) {
      logger.error("System variable {} is not set", properties.envPasswordVariable());
      error = true;
    }
    if (error) {
      throw new RuntimeException("an error happened during the retrieval of system variables");
    }
    tokenRequestBody.add("username", apiUsername);
    tokenRequestBody.add("password", apiPassword);
    tokenRequestBody.add("grant_type", "password");
    tokenRequestBody.add("client_id", "s21-open-api");
    reqBucket = Bucket.builder()
      .addLimit(b -> b
        .capacity(1)
        .refillGreedy(properties.rateLimit(), Duration.ofSeconds(1)))
      .build();
    apiBaseUrl = properties.apiBaseUrl();
    apiClient = apiClientBuilder
        .build();
    requestExecutor = Executors.newFixedThreadPool(properties.rateLimit());
  }
  public void updateApiKey() {
    if (System.currentTimeMillis() >= keyExpiryDate) {
      logger.info("API key is out of date (current timestamp is {}), key expiry timestamp is {}", System.currentTimeMillis(), keyExpiryDate);
      logger.info("updating API key...");
      RestClient keyApiReqClient = restClientBuilder
        .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
        .build();
      ApiKeyResponse keyEntity = keyApiReqClient.post()
        .uri(tokenEndpointUrl)
        .body(tokenRequestBody)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            ((request, response) ->
                logger.error("couldn't update API key, request = {}, response = {}",
                    request,
                    response)
            )
        )
          .body(ApiKeyResponse.class);
      if (keyEntity != null) {
        keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn() * 1000L;
        logger.info("successfully updated API key, new key expiry date = {}", LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiryDate), TimeZone.getDefault().toZoneId()));
        apiClient = apiClient.mutate().defaultHeader("Authorization", "Bearer " + keyEntity.accessToken()).build();
      }
    }
  }
  public <T> T request(Class<T> responseClass, String apiUrl) {
    updateApiKey();
    Future<T> f = requestExecutor.submit(() -> {
      while (true) {
        if (reqBucket.tryConsume(1)) {
          RetryTemplate template = RetryTemplate.builder()
              .maxAttempts(10)
              .retryOn(HttpClientErrorException.class)
              .retryOn(IOException.class)
              .retryOn(org.springframework.web.client.ResourceAccessException.class)
              .build();
          return template.execute(_ -> apiClient.get()
              .uri(apiBaseUrl + apiUrl)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                logger.error("error {}, retrying", resp.getStatusCode());
                throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
              })
              .body(responseClass));
        }
      }
    });
    try {
      return f.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
