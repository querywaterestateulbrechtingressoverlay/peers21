package com.example.demo.scraper;

import com.example.demo.scraper.dto.ApiKeyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApiRequestService {
  static class Builder {
    private String apiBaseUrl = "";
    private String tokenEndpointUrl = "";
    private String envUsernameVariable = "user";
    private String envPasswordVariable = "password";
    private int rateLimit = 3;

    public Builder apiBaseUrl(String apiBaseUrl) {
      this.apiBaseUrl = apiBaseUrl;
      return this;
    }

    public Builder tokenEndpointUrl(String tokenEndpointUrl) {
      this.tokenEndpointUrl = tokenEndpointUrl;
      return this;
    }

    public Builder envUsernameVariable(String envUsernameVariable) {
      this.envUsernameVariable = envUsernameVariable;
      return this;
    }

    public Builder envPasswordVariable(String envPasswordVariable) {
      this.envPasswordVariable = envPasswordVariable;
      return this;
    }

    public Builder rateLimit(int rateLimit) {
      this.rateLimit = rateLimit;
      return this;
    }
    public ApiRequestService build() {
      return new ApiRequestService(apiBaseUrl, tokenEndpointUrl, envUsernameVariable, envPasswordVariable, rateLimit);
    }
  }
  private final String tokenEndpointUrl;
  private final Bucket reqBucket;
  private final Logger logger = LoggerFactory.getLogger(ApiRequestService.class);
  private final LinkedMultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
  private String apiKey = "";
  private long keyExpiryDate = System.currentTimeMillis();
  private RestClient apiReqClient;
  private ExecutorService requestExecutor;

  ApiRequestService(String apiBaseUrl, String tokenEndpointUrl, String envUsernameVariable, String envPasswordVariable, int rateLimit) {
    this.tokenEndpointUrl = tokenEndpointUrl;
    boolean error = false;
    logger.info("retrieving API username from environment variables...");
    String apiUsername = System.getenv(envUsernameVariable);
    if (apiUsername == null) {
      logger.error("System variable {} is not set", envUsernameVariable);
      error = true;
    }
    logger.info("retrieving API password from environment variables...");
    String apiPassword = System.getenv(envPasswordVariable);
    if (apiPassword == null) {
      logger.error("System variable {} is not set", envPasswordVariable);
      error = true;
    }
    if (error) {
      throw new RuntimeException("an error happened during the retrieval of system variables");
    }
    reqBucket = Bucket.builder()
      .addLimit(b -> b
        .capacity(1)
        .refillGreedy(rateLimit, Duration.ofSeconds(1)))
      .build();
    apiReqClient = RestClient.builder()
      .baseUrl(apiBaseUrl)
      .build();
    tokenRequestBody.add("username", apiUsername);
    tokenRequestBody.add("password", apiPassword);
    tokenRequestBody.add("grant_type", "password");
    tokenRequestBody.add("client_id", "s21-open-api");
    requestExecutor = Executors.newFixedThreadPool(rateLimit);
  }
  public static ApiRequestService.Builder getBuilder() {
    return new ApiRequestService.Builder();
  }
  public void updateApiKey() {
    if (System.currentTimeMillis() >= keyExpiryDate) {
      logger.info("API key is out of date (current timestamp is {}), key expiry timestamp is {}", System.currentTimeMillis(), keyExpiryDate);
      logger.info("updating API key...");
      RestClient keyApiReqClient = RestClient.builder()
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
        apiKey = keyEntity.accessToken();
        keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn() * 1000L;
        logger.info("successfully updated API key, new key expiry date = {}", LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiryDate), TimeZone.getDefault().toZoneId()));
        apiReqClient = apiReqClient.mutate().defaultHeader("Authorization", "Bearer " + apiKey).build();
      } else {
        apiKey = "";
      }
    }
  }
  <T> T request(Class<T> responseClass, String apiUrl) {
    AtomicBoolean tooManyRequests = new AtomicBoolean(false);
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
          return template.execute(ctx -> apiReqClient.get()
              .uri(apiUrl)
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
