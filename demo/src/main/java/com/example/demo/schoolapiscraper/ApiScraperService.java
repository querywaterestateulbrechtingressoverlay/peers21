package com.example.demo.schoolapiscraper;

import com.example.demo.data.Peer;
import com.example.demo.data.PeerRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Date;

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

    private final String tokenUrl = "";
    private final String apiUrl = "";
    private TokenRequestBody trb;
    private String apiKey = "";
    private long lastUpdateDate = System.currentTimeMillis();
    private long keyExpiryDate = System.currentTimeMillis();
    @Autowired
    PeerRepository repo;
    ApiScraperService() {
        String apiUsername = System.getenv("API_USERNAME");
        String apiPassword = System.getenv("API_PASSWORD");
        if (apiUsername.isEmpty()) {
            throw new RuntimeException("System variable API_USERNAME is not set");
        }
        if (apiPassword.isEmpty()) {
            throw new RuntimeException("System variable API_PASSWORD is not set");
        }
        trb = new TokenRequestBody(apiUsername, apiPassword);
    }
    @Bean
    public ApiScraperService apiScraper() {
        return new ApiScraperService();
    }
    public void updateApiKey() {
        RestClient apiReqClient = RestClient.builder()
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        ApiKeyResponse keyEntity = apiReqClient.post()
                .uri(tokenUrl)
                .body(trb)
                .retrieve()
                .body(ApiKeyResponse.class);
        if (keyEntity != null) {
            apiKey = keyEntity.accessToken;
            keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn * 1000L;
        } else {
            apiKey = "";
        }
    }

    @Scheduled(fixedRateString = "PT15M")
    void updatePeerList() {
        if (!apiKey.isEmpty()) {
            RestClient apiReqClient = RestClient.builder()
                    .baseUrl(apiUrl)
                    .build();
            PeerResponse
            for (Peer p : repo.getAllPeers()) {
                while (!success) {
                    apiReqClient.get()
                            .uri(apiUrl + "/" + p.name() + "@student.21-school.ru")
                            .retrieve()
                            .body()

                }

            }
        }
    }
    public Date getLastUpdateDate() {
        return new Date(lastUpdateDate);
    }
}
