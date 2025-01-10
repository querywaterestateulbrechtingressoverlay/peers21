package com.example.demo;

import com.example.demo.scraper.ApiRequestService;
import com.example.demo.scraper.ApiRequestServiceProperties;
import com.example.demo.scraper.dto.ApiKeyResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = ApiRequestService.class)
public class ApiRequestTests {

  @Autowired
  ApiRequestService requestService;
  @TestBean
  private ApiRequestServiceProperties properties;
  static ApiRequestServiceProperties properties() {
    return new ApiRequestServiceProperties(
        "http://localhost/api-endpoint",
        "http://localhost/token-endpoint",
        "API_USERNAME",
        "API_PASSWORD",
        3);
  }
  RestTemplate restTemplate = new RestTemplate();
  MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();
  ObjectMapper objMapper = new ObjectMapper();

  @Test
  void testApiTokenRequest() {
    Random rng = new Random();
    ApiKeyResponse fastExpiryMockKey = new ApiKeyResponse(
        "cooltoken" + rng.nextInt(),
        10, 10,
        "anothercooltoken" + rng.nextInt(),
        "Bearer", "1",
        "qwerty", "profile email");
    MultiValueMap<String, String> expectedPostRequest = new LinkedMultiValueMap<>();
    expectedPostRequest.add("client_id", "s21-open-api");
    expectedPostRequest.add("username", System.getenv(properties.envUsernameVariable()));
    expectedPostRequest.add("password", System.getenv(properties.envPasswordVariable()));
    expectedPostRequest.add("grant_type", "password");
    try {
      mockServer
        .expect(requestTo(new URI(properties.tokenEndpointUrl())))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Content-Type", "application/x-www-form-urlencoded"))
        .andExpect(content().formData(expectedPostRequest))
        .andRespond(withSuccess(objMapper.writeValueAsString(fastExpiryMockKey), MediaType.APPLICATION_JSON));
    } catch (JsonProcessingException ignored) {

    } catch (URISyntaxException e) {

    }
    requestService.updateApiKey();
  }
}
