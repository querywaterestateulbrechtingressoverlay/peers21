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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@EnableScheduling
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
            logger.error("System variable API_PASSWORD is not set");
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
//    @Bean
//    public ApiScraperService apiScraper() {
//        return new ApiScraperService();
//    }
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

    boolean diff(Peer p, PeerResponse pr, PeerPointsResponse ppr) {
        return p.state().name().equals(pr.status()) && p.xp() == pr.expValue() && p.peerReviewPoints() == ppr.peerReviewPoints() && p.codeReviewPoints() == ppr.codeReviewPoints() && p.coins() == ppr.coins();
    }

    Peer updatedPeer(Peer p, PeerResponse pr, PeerPointsResponse ppr) {
        return new Peer(p.id(), p.name(), PeerState.valueOf(pr.status()), p.wave(), p.intensive(), pr.expValue(), ppr.peerReviewPoints(), ppr.codeReviewPoints(), ppr.coins());
    }

    @Scheduled(fixedRateString = "PT1M")
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
            AtomicInteger counter = new AtomicInteger(0);
            Iterator<Peer> peerIterator = peerList.iterator();
            try (ScheduledExecutorService requestExecutor = Executors.newScheduledThreadPool(3)) {
                AtomicBoolean done = new AtomicBoolean(false);
                logger.info("ASD");
                ScheduledFuture<?> f = requestExecutor.scheduleAtFixedRate(() -> {
                    counter.incrementAndGet();
                    if (counter.get() == peerList.size()) {
                        requestExecutor.shutdown();
                    }
                    Peer currentPeer = peerIterator.next();
                    logger.info("peer " + currentPeer.name());
                    try {
                        logger.info("ASD");
                        PeerResponse peerResponse = apiReqClient.get()
                                .uri(apiUrl + "/participants/" + currentPeer.name())
                                .retrieve()
                                .body(PeerResponse.class);
                        PeerPointsResponse peerPointsResponse = apiReqClient.get()
                                .uri(apiUrl + "/participants/" + currentPeer.name() + "/points")
                                .retrieve()
                                .body(PeerPointsResponse.class);
                        if (!diff(currentPeer, peerResponse, peerPointsResponse)) {
                            changedPeers.add(updatedPeer(currentPeer, peerResponse, peerPointsResponse));
                        }
                    } catch (RestClientResponseException e) {
                        logger.error("received error " + e.getStatusCode() + ", message = " + e.getResponseBodyAsString());
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }, 0, 400, TimeUnit.MILLISECONDS);
            }
            if (!changedPeers.isEmpty()) {
                repo.saveAll(changedPeers);
                logger.info("update finished, updated " + changedPeers.size() + " peers");
            } else {
                logger.info("no peers were updated");
            }
        } else {
            logger.warn("no API key found, update stopped");
        }
    }
    public Date getLastUpdateDate() {
        return new Date(lastUpdateDate);
    }

}
