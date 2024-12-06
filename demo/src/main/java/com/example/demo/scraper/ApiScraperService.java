package com.example.demo.scraper;

import com.example.demo.data.Peer;
import com.example.demo.data.PeerRepository;
import com.example.demo.scraper.dto.ApiKeyResponse;
import com.example.demo.scraper.dto.PeerResponse;
import com.example.demo.scraper.dto.TokenRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class ApiScraperService {

    private final String tokenUrl = "";
    private final String apiUrl = "";
    private final LinkedMultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
    private String apiKey = "";
    private final long lastUpdateDate = System.currentTimeMillis();
    private long keyExpiryDate = System.currentTimeMillis();
    Logger logger = LoggerFactory.getLogger(ApiScraperService.class);
//    @Autowired
//    PeerRepository repo;
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
            logger.error("System variable API_USERNAME is not set");
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
    @Bean
    public ApiScraperService apiScraper() {
        return new ApiScraperService();
    }
    public void updateApiKey() {
        logger.info("updating API key...");
        RestClient apiReqClient = RestClient.builder()
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        ApiKeyResponse keyEntity = apiReqClient.post()
                .uri(tokenUrl)
                .body(tokenRequestBody)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> logger.error("couldn't update API key, request = " + request + ", response = " + response)))
                .body(ApiKeyResponse.class);
        if (keyEntity != null) {
            apiKey = keyEntity.accessToken();
            keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn() * 1000L;
            logger.info("successfully updated API key, new key expiry date = " + LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiryDate), TimeZone.getDefault().toZoneId()));
            logger.info("key = " + apiKey);
        } else {
            apiKey = "";
        }
    }



//    @Scheduled(fixedRateString = "PT15M")
//    boolean updatePeerList() {
//        logger.info("updating peer info...");
//        if (System.currentTimeMillis() <= keyExpiryDate) {
//            logger.info("API key is out of date, updating...");
//            updateApiKey();
//        }
//        logger.info("starting peer list update...");
//        if (!apiKey.isEmpty()) {
//            RestClient apiReqClient = RestClient.builder()
//                    .baseUrl(apiUrl)
//                    .build();
//            List<Peer> peerList = repo.getAllPeers();
//            try (ScheduledExecutorService requestExecutor = Executors.newSingleThreadScheduledExecutor()) {
//                for (Peer p : peerList) {
//                    final boolean[] tooManyRequests = {false};
//                    Callable<PeerResponse> cpr = () -> {
//                        PeerResponse pr = apiReqClient.get()
//                                .uri(apiUrl + "/" + p.name() + "@student.21-school.ru")
//                                .retrieve()
//                                .onStatus((hsc) -> hsc == HttpStatus.TOO_MANY_REQUESTS, (req, resp) -> tooManyRequests[0] = true)
//                                .body(PeerResponse.class);
//                        return pr;
//                    };
//                    requestExecutor.schedule(cpr, 1);
//                }
//
//
//            }
//            for (Peer p : peerList) {
//                PeerResponse pr = apiReqClient.get()
//                        .uri(apiUrl + "/" + p.name() + "@student.21-school.ru")
//                        .retrieve()
//                        .onStatus((hsc) -> hsc == HttpStatus.TOO_MANY_REQUESTS, (req, resp) -> tooManyRequests[0] = true)
//                        .body(PeerResponse.class);
//                while (tooManyRequests[0]) {
//                    Timer t = new Timer();
//
//                }
//                if (pr != null) {
//                    PeerPointsResponce
//                }
//            }
//        } else {
//            logger.warn("no API key found, update stopped");
//        }
//    }
    public Date getLastUpdateDate() {
        return new Date(lastUpdateDate);
    }
}
