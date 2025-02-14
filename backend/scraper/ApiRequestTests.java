//package ru.cyphercola.peers21.backend;
//
//import com.example.demo.scraper.ApiRequestService;
//import com.example.demo.scraper.ApiRequestServiceProperties;
//import com.example.demo.scraper.dto.ApiKeyResponse;
//import com.example.demo.scraper.dto.CampusDTO;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.bean.override.convention.TestBean;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestClient;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
//import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
//
//@SpringBootTest(classes = {ApiRequestService.class})
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class ApiRequestTests {
//  @Autowired
//  ApiRequestService requestService;
//  @TestBean
//  private ApiRequestServiceProperties properties;
//  static ApiRequestServiceProperties properties() {
//    return new ApiRequestServiceProperties(
//        "http://localhost/api-endpoint",
//        "http://localhost/token-endpoint",
//        "API_USERNAME",
//        "API_PASSWORD",
//        3);
//  }
//  @TestBean
//  private RestClient.Builder apiReqClientBuilder;
//  static RestClient.Builder apiReqClientBuilder() {
//    return RestClient.builder();
//  }
//
//  MockRestServiceServer mockServer;
//
//  ObjectMapper objMapper = new ObjectMapper();
//  ApiKeyResponse tokenResponse = new ApiKeyResponse(
//      "cooltoken",
//      10, 10,
//      "anothercooltoken",
//      "Bearer", "1",
//      "qwerty", "profile email");
//
//  @BeforeEach
//  void reset() {
//    mockServer = MockRestServiceServer.bindTo(apiReqClientBuilder).ignoreExpectOrder(true).build();
//  }
//
//  @Test
//  void testApiTokenRequest() throws URISyntaxException, JsonProcessingException {
//    var expectedPostRequest = new LinkedMultiValueMap<String, String>();
//    expectedPostRequest.add("client_id", "s21-open-api");
//    expectedPostRequest.add("username", System.getenv(properties.envUsernameVariable()));
//    expectedPostRequest.add("password", System.getenv(properties.envPasswordVariable()));
//    expectedPostRequest.add("grant_type", "password");
//    mockServer
//      .expect(requestTo(new URI(properties.tokenEndpointUrl())))
//      .andExpect(method(HttpMethod.POST))
//      .andExpect(header("Content-Type", "application/x-www-form-urlencoded"))
//      .andExpect(content().formData(expectedPostRequest))
//      .andRespond(withSuccess(objMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));
//    requestService.updateApiKey();
//    mockServer.verify();
//  }
//  @Test
//  void testSimpleApiRequest() throws JsonProcessingException, URISyntaxException {
//    MultiValueMap<String, String> expectedPostRequest = new LinkedMultiValueMap<>();
//    expectedPostRequest.add("client_id", "s21-open-api");
//    expectedPostRequest.add("username", System.getenv(properties.envUsernameVariable()));
//    expectedPostRequest.add("password", System.getenv(properties.envPasswordVariable()));
//    expectedPostRequest.add("grant_type", "password");
//    CampusDTO testResponse = new CampusDTO("cool-id", "ykt", "yakutsk");
//    mockServer
//      .expect(requestTo(new URI(properties.tokenEndpointUrl())))
//      .andExpect(method(HttpMethod.POST))
//      .andExpect(header("Content-Type", "application/x-www-form-urlencoded"))
//      .andExpect(content().formData(expectedPostRequest))
//      .andRespond(withSuccess(objMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));
//    mockServer
//      .expect(requestTo(new URI(properties.apiBaseUrl() + "/campus")))
//      .andRespond(withSuccess(objMapper.writeValueAsString(testResponse), MediaType.APPLICATION_JSON));
//    requestService.request(CampusDTO.class, "/campus");
//    mockServer.verify();
//  }
//}
