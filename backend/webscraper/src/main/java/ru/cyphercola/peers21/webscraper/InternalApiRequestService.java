package ru.cyphercola.peers21.webscraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Base64;

@Service
@EnableConfigurationProperties(InternalApiRequestServiceProperties.class)
public class InternalApiRequestService {
  Logger logger = LoggerFactory.getLogger(InternalApiRequestService.class);
  private final String authorization;
  private final String apiBaseUrl;
  private final RestClient apiClient;

  @Autowired
  InternalApiRequestService(@Autowired RestClient.Builder rcb, InternalApiRequestServiceProperties properties) {
    logger.info("username = {}, password = {}", properties.apiUsername(), properties.apiPassword());
    this.authorization = "Basic " + Base64.getEncoder().encodeToString((properties.apiUsername() + ":" + properties.apiPassword()).getBytes());
    this.apiBaseUrl = properties.apiBaseUrl();
    apiClient = rcb
      .defaultHeader("Authorization", authorization)
      .build();
  }
  public <T> T get(Class<T> responseClass, String apiUrl) {
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
  }
  public <U> void put(U requestBody, String apiUrl) {
    var asd = apiClient.put()
      .uri(apiBaseUrl + apiUrl)
      .header("Authorization", authorization)
      .body(requestBody)
      .retrieve()
      .toBodilessEntity();
    System.out.println(asd.getStatusCode());
    logger.info(asd.getStatusCode().toString());
  }
}
