package com.example.demo;

import com.example.demo.scraper.ApiRequestServiceProperties;
import com.example.demo.scraper.dto.ApiKeyResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Random;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = DemoApplication.class)
@EnableWebMvc
public class BasicFunctionalityTests {
  @Autowired
  private MockMvc mockMvc;

  @TestBean
  private ApiRequestServiceProperties properties;
  static ApiRequestServiceProperties properties() {
    return new ApiRequestServiceProperties(
        "api-endpoint",
        "token-endpoint",
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
//    try {
//      mockServer
//          .expect(requestTo(properties.tokenEndpointUrl()))
//          .andRespond(withSuccess(objMapper.writeValueAsString(fastExpiryMockKey), MediaType.APPLICATION_JSON));
//    } catch (JsonProcessingException ignored) {
//
//    }

  }
}
