package com.example.demo.schoolapiscraper;

import com.example.demo.data.Peer;
import com.example.demo.data.PeerRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class ApiScraperService {
    static class TokenRequestBody {
        String username;
        String password;
        String grant_type = "password";
        String client_id = "s21-open-api";
        TokenRequestBody(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
    static class ApiKeyResponse {
        @JsonProperty("access_token")
        String accessToken;
        @JsonProperty("expires_in")
        int expiresIn;
        @JsonProperty("refresh_expires_in")
        int refreshExpiresIn;
        @JsonProperty("refresh_token")
        String refreshToken;
        @JsonProperty("token_type")
        String tokenType;
        @JsonProperty("not-before-policy")
        String notBeforePolicy;
        @JsonProperty("session_state")
        String sessionState;
        String scope;


        ApiKeyResponse(String accessToken, int expiresIn, int refreshExpiresIn, String refreshToken, String tokenType, String notBeforePolicy, String sessionState, String scope) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
            this.refreshExpiresIn = refreshExpiresIn;
            this.refreshToken = refreshToken;
            this.tokenType = tokenType;
            this.notBeforePolicy = notBeforePolicy;
            this.sessionState = sessionState;
            this.scope = scope;
        }
    }

    static class PeerResponse {
        static class Campus {
            String id;
            String shortName;

            public Campus(String id, String shortName) {
                this.id = id;
                this.shortName = shortName;
            }
        }
        String login;
        String className;
        String parallelName;
        int expValue;
        int level;
        int expToNextLevel;
        Campus campus;
        String status;

        public PeerResponse(String login, String className, String parallelName, int expValue, int level, int expToNextLevel, Campus campus, String status) {
            this.login = login;
            this.className = className;
            this.parallelName = parallelName;
            this.expValue = expValue;
            this.level = level;
            this.expToNextLevel = expToNextLevel;
            this.campus = campus;
            this.status = status;
        }
    }

    private final String tokenUrl = "https://auth.sberclass.ru/auth/realms/EduPowerKeycloak/protocol/openid-connect/token";
    private final String apiUrl = "https://edu-api.21-school.ru/services/21-school/api/v1";
    private TokenRequestBody trb;
    private String apiKey = "";
    private long lastUpdateDate = System.currentTimeMillis();
    private long keyExpiryDate = System.currentTimeMillis();
    Logger logger = LoggerFactory.getLogger(ApiScraperService.class);
    @Autowired
    PeerRepository repo;
    ApiScraperService() {
        boolean error = false;
        logger.info("retrieving API username from environment variables...");
        String apiUsername = System.getenv("API_USERNAME");
        if (apiUsername.isEmpty()) {
            logger.error("System variable API_USERNAME is not set");
            error = true;
        }
        logger.info("retrieving API password from environment variables...");
        String apiPassword = System.getenv("API_PASSWORD");
        if (apiPassword.isEmpty()) {
            logger.error("System variable API_USERNAME is not set");
            error = true;
        }
        if (error) {
            throw new RuntimeException("an error happened during the retrieval of system variables");
        } else {
            trb = new TokenRequestBody(apiUsername, apiPassword);
            logger.info("successfully obtained API username and password");
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
                .body(trb)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> logger.error("couldn't update API key, request = " + request + ", response = " + response)))
                .body(ApiKeyResponse.class);
        if (keyEntity != null) {
            apiKey = keyEntity.accessToken;
            keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn * 1000L;
            logger.info("successfully updated API key, new key expiry date = " + LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiryDate), TimeZone.getDefault().toZoneId()));
        } else {
            apiKey = "";
        }
    }

    @Scheduled(fixedRateString = "PT15M")
    boolean updatePeerList() {
        logger.info("updating peer info...");
        if (System.currentTimeMillis() <= keyExpiryDate) {
            logger.info("API key is out of date, updating...");
            updateApiKey();
        }
        logger.info("starting peer list update...");
        if (!apiKey.isEmpty()) {
            RestClient apiReqClient = RestClient.builder()
                    .baseUrl(apiUrl)
                    .build();
            List<Peer> peerList = repo.getAllPeers();
            for (Peer p : peerList) {
                boolean success = true;
                apiReqClient.get()
                        .uri(apiUrl + "/" + p.name() + "@student.21-school.ru")
                        .retrieve()
                        .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {

                        })
                        .body()

            }
        } else {
            logger.warn("no API key found, update stopped");
        }
    }
    public Date getLastUpdateDate() {
        return new Date(lastUpdateDate);
    }
}
