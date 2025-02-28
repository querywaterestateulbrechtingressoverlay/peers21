package ru.cyphercola.peers21.webscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import ru.cyphercola.peers21.webscraper.dto.internal.JWTokenDTO;

import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

@Service
@EnableConfigurationProperties(InternalApiRequestServiceProperties.class)
public class InternalApiRequestService {
  private final Logger logger = LoggerFactory.getLogger(InternalApiRequestService.class);
  private final String apiBaseUrl;
  private RestClient tokenClient;
  private RestClient apiClient;

  private Instant tokenExpiryTimestamp;

  @Autowired
  InternalApiRequestService(@Autowired RestClient.Builder rcb, InternalApiRequestServiceProperties properties) {
    tokenClient = rcb.defaultHeader("Authorization",
        "Basic " +
            Base64.getEncoder().encodeToString(
                (properties.apiUsername() +
                ":" +
                properties.apiPassword()
            ).getBytes()))
        .build();
    this.apiBaseUrl = properties.apiBaseUrl();
    tokenExpiryTimestamp = Instant.now().minusSeconds(1);
    apiClient = rcb.build();
  }

  void updateAuthToken() {
    if (tokenExpiryTimestamp.isBefore(Instant.now())) {
      logger.info("data layer token has expired, updating...");
      JWTokenDTO tokenResponse = tokenClient.post()
          .uri(apiBaseUrl + "/auth/login")
          .retrieve()
          .body(JWTokenDTO.class);
      tokenExpiryTimestamp = Instant.now().plusSeconds(tokenResponse.expirySeconds());
      apiClient = apiClient.mutate()
          .defaultHeader("Authorization", "Bearer " + tokenResponse.token())
          .build();
      logger.info("token update successful, new token = {}, expires at {}", tokenResponse.token(), tokenExpiryTimestamp.toString());
    }
  }

  public <T> T get(Class<T> responseClass, String apiUrl) {
    updateAuthToken();
    try {
      logger.info("GET request to internal API {}", apiBaseUrl + apiUrl);
      T result;
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
        .body(responseClass));
      return result;
    } catch (HttpStatusCodeException e) {
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED && e.getResponseHeaders().containsKey("WWW-Authenticate")) {
        logger.warn("{}", e.getResponseHeaders().get("WWW-Authenticate"));
      }
      logger.warn("error while making a GET request to internal api {}, status code = {}, status text = {}", apiUrl, e.getStatusCode(), e.getStatusText());
      return null;
    }
  }
  public <U> void put(U requestBody, String apiUrl) {
    updateAuthToken();
    try {
      logger.info("PUT request to internal API {}", apiBaseUrl + apiUrl);
      var asd = apiClient.put()
        .uri(apiBaseUrl + apiUrl)
        .body(requestBody)
        .retrieve()
        .toBodilessEntity();
    } catch (HttpStatusCodeException e) {
      logger.warn("error while making a PUT request to internal api {}, status code = {}, status text = {}", apiUrl, e.getStatusCode(), e.getStatusText());
      throw e;
    }
  }
}
