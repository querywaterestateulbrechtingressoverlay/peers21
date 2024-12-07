package com.example.demo.scraper;

import com.example.demo.data.Peer;
import com.example.demo.data.PeerRepository;
import com.example.demo.data.PeerState;
import com.example.demo.scraper.dto.ApiKeyResponse;
import com.example.demo.scraper.dto.PeerPointsResponse;
import com.example.demo.scraper.dto.PeerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.*;

@Service
public class ApiScraperService {

    private final String apiUrl = "";
    private final LinkedMultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
    private String apiKey = "";
    private final long lastUpdateDate = System.currentTimeMillis();
    private long keyExpiryDate = System.currentTimeMillis();
    Logger logger = LoggerFactory.getLogger(ApiScraperService.class);
    @Autowired
    PeerRepository repo;
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
        String tokenUrl = "https://auth.sberclass.ru/auth/realms/EduPowerKeycloak/protocol/openid-connect/token";
        ApiKeyResponse keyEntity = apiReqClient.post()
                .uri(tokenUrl)
                .body(tokenRequestBody)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> logger.error("couldn't update API key, request = " + request + ", response = " + response)))
                .body(ApiKeyResponse.class);
        if (keyEntity != null) {
            apiKey = keyEntity.accessToken();
            logger.info("expires_in = " + keyEntity.expiresIn());
            keyExpiryDate = System.currentTimeMillis() + keyEntity.expiresIn() * 1000L;
            logger.info("successfully updated API key, new key expiry date = " + LocalDateTime.ofInstant(Instant.ofEpochMilli(keyExpiryDate), TimeZone.getDefault().toZoneId()));
            logger.info("key = " + apiKey);
        } else {
            apiKey = "";
        }
    }
    @Scheduled(fixedRateString = "PT15M")
    public void updatePeerList() {
        logger.info("updating peer info...");
        if (System.currentTimeMillis() >= keyExpiryDate) {
            logger.info("API key is out of date, updating... (current timestamp is " + System.currentTimeMillis() + "), key expiry timestamp is " + keyExpiryDate);
            updateApiKey();
        }
        logger.info("starting peer list update...");
        if (!apiKey.isEmpty()) {
            RestClient apiReqClient = RestClient.builder()
                    .baseUrl(apiUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .build();
            var peerList = repo.getAllPeers();
            var changedPeers = new ArrayList<Peer>();
            try (ScheduledExecutorService requestExecutor = Executors.newSingleThreadScheduledExecutor()) {
                for (Peer p : peerList) {
                    logger.info("peer " + p.name());
                    Callable<PeerResponse> cpr = () -> apiReqClient.get()
                            .uri(apiUrl + "/participants/" + p.name())
                            .retrieve()
                            .body(PeerResponse.class);
                    Callable<PeerPointsResponse> cppr = () -> apiReqClient.get()
                            .uri(apiUrl + "/participants/" + p.name() + "/points")
                            .retrieve()
                            .body(PeerPointsResponse.class);
                    ScheduledFuture<PeerResponse> asd = requestExecutor.schedule(cpr, 1000, TimeUnit.MILLISECONDS);
                    ScheduledFuture<PeerPointsResponse> dsa = requestExecutor.schedule(cppr, 1000, TimeUnit.MILLISECONDS);
                    PeerResponse pr = asd.get();
                    PeerPointsResponse ppr = dsa.get();
                    logger.info("response received");
                    if (p.state() != PeerState.valueOf(pr.status()) || p.xp() != pr.expValue() || ppr.peerReviewPoints() != p.peerReviewPoints() || ppr.codeReviewPoints() != p.codeReviewPoints() || ppr.coins() != p.coins()) {
                        logger.info("values received differ from values in database, updating...");
                        changedPeers.add(new Peer(p.id(), p.name(), p.state(), p.wave(), p.intensive(), pr.expValue(), ppr.peerReviewPoints(), ppr.codeReviewPoints(), ppr.coins()));
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!changedPeers.isEmpty()) {
                repo.saveAll(changedPeers);
                logger.info("update finished, updated " + changedPeers.size() + " peers");
            }
        } else {
            logger.warn("no API key found, update stopped");
        }
    }
    public Date getLastUpdateDate() {
        return new Date(lastUpdateDate);
    }

}
