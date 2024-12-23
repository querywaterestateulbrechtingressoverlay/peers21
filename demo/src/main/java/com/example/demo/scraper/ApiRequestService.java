package com.example.demo.scraper;

import com.example.demo.scraper.dto.ApiKeyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ApiRequestService {
  private static class Builder {
    private String apiBaseUrl = "";
    private String tokenEndpointUrl = "";
    private String envUsernameVariable;
    private String envPasswordVariable;
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
    public ApiRequestService builder() {
      return new ApiRequestService(apiBaseUrl, tokenEndpointUrl, envUsernameVariable, envPasswordVariable, rateLimit);
    }
  }
  private final String tokenEndpointUrl;
  private final String apiUsername;
  private final String apiPassword;
  private final Bucket reqBucket;
  private final Logger logger = LoggerFactory.getLogger(ApiRequestService.class);
  private final LinkedMultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
  private String apiKey = "";
  private long keyExpiryDate = System.currentTimeMillis();
  private RestClient apiReqClient;

  ApiRequestService(String apiBaseUrl, String tokenEndpointUrl, String envUsernameVariable, String envPasswordVariable, int rateLimit) {
    this.tokenEndpointUrl = tokenEndpointUrl;
    boolean error = false;
    apiUsername = System.getenv("API_USERNAME");
    if (apiUsername == null) {
      logger.error("System variable API_USERNAME is not set");
      error = true;
    }
    logger.info("retrieving API password from environment variables...");
    apiPassword = System.getenv("API_PASSWORD");
    if (apiPassword == null) {
      logger.error("System variable API_PASSWORD is not set");
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
  }
  ApiRequestService.Builder getBuilder() {
    return new ApiRequestService.Builder();
  }
  public void updateApiKey() {
    if (System.currentTimeMillis() >= keyExpiryDate) {
      logger.info("API key is out of date (current timestamp is " + System.currentTimeMillis() + "), key expiry timestamp is " + keyExpiryDate);
      logger.info("updating API key...");
      RestClient keyApiReqClient = RestClient.builder()
        .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
        .build();
      ApiKeyResponse keyEntity = keyApiReqClient.post()
        .uri(tokenEndpointUrl)
        .body(tokenRequestBody)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> logger.error("couldn't update API key, request = " + request + ", response = " + response)))
        .body(ApiKeyResponse.class);
      if (keyEntity != null) {
        apiKey = keyEntity.accessToken();
        keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn() * 1000L;
        logger.info("successfully updated API key, new key expiry date = " + LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiryDate), TimeZone.getDefault().toZoneId()));
        apiReqClient = apiReqClient.mutate().defaultHeader("Authorization", "Bearer " + apiKey).build();
      } else {
        apiKey = "";
      }
    }
  }
  <T> T request(Class<T> responseClass, String apiUrl) {
    updateApiKey();
    AtomicBoolean tooManyRequests = new AtomicBoolean(false);
    T returnValue;
    while (true) {
      if (reqBucket.tryConsume(1)) {
        returnValue = apiReqClient.get()
          .uri(apiUrl)
          .accept(MediaType.APPLICATION_JSON)
          .exchange((req, resp) -> {
            if (resp.getStatusCode() != HttpStatus.OK) {
              if (resp.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                tooManyRequests.set(true);
              } else {
                throw new RestClientResponseException(req.getMethod().toString() + req.getURI(), resp.getStatusCode(), resp.getStatusText(), req.getHeaders(), resp.getBody().readAllBytes(), Charset.defaultCharset());
              }
            }
            return Objects.requireNonNull(resp.bodyTo(responseClass));
          });
        if (tooManyRequests.get()) {
          continue;
        }
        break;
      }
    }
    return returnValue;
  }
}
