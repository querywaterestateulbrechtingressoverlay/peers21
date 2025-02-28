package ru.cyphercola.peers21.webscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.cyphercola.peers21.webscraper.dto.external.ApiKeyResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Service
@EnableConfigurationProperties(ExternalApiRequestServiceProperties.class)
public class ExternalApiRequestService {
  private final String tokenEndpointUrl;
  private final String apiBaseUrl;
  private final Bucket reqBucket;
  private final Logger logger = LoggerFactory.getLogger(ExternalApiRequestService.class);
  private final LinkedMultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
  private long keyExpiryDate = System.currentTimeMillis();
  private RestClient apiClient;

  @Autowired
  private RestClient.Builder restClientBuilder;

  void rebuildClient() {
    if (apiClient == null) {
      apiClient = restClientBuilder.build();
    }
  }

  @Autowired
  ExternalApiRequestService(ExternalApiRequestServiceProperties properties) {
    this.tokenEndpointUrl = properties.tokenEndpointUrl();
    tokenRequestBody.add("username", properties.apiUsername());
    tokenRequestBody.add("password", properties.apiPassword());
    tokenRequestBody.add("grant_type", "password");
    tokenRequestBody.add("client_id", "s21-open-api");
    reqBucket = Bucket.builder()
      .addLimit(b -> b
        .capacity(1)
        .refillGreedy(properties.rateLimit(), Duration.ofSeconds(1)))
      .build();
    apiBaseUrl = properties.apiBaseUrl();
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
      rebuildClient();
      keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn() * 1000L;
      logger.info("successfully updated API key, new key expiry date = {}", LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiryDate), TimeZone.getDefault().toZoneId()));
      apiClient = apiClient.mutate().defaultHeader("Authorization", "Bearer " + keyEntity.accessToken()).build();
    }
  }
  public <T> T get(Class<T> responseClass, String apiUrl) {
    updateApiKey();
    rebuildClient();
    T result;
    while (true) {
      if (reqBucket.tryConsume(1)) {
        RetryTemplate template = RetryTemplate.builder()
          .maxAttempts(10)
          .retryOn(HttpClientErrorException.class)
          .retryOn(IOException.class)
          .retryOn(org.springframework.web.client.ResourceAccessException.class)
          .build();
        result = template.execute(r -> apiClient.get()
          .uri(apiBaseUrl + apiUrl)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
            logger.trace("error {}, retrying", resp.getStatusCode());
            throw new HttpClientErrorException(resp.getStatusCode());
          })
          .body(responseClass));
        break;
      }
    }
    return result;
  }
}
