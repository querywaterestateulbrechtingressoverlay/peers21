package com.example.demo.scraper;

import com.example.demo.data.*;
import com.example.demo.scraper.dto.ApiCampusesDTO;
import com.example.demo.scraper.dto.ApiParticipantLoginsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.StreamSupport;

@EnableScheduling
@Service
public class ApiScraperService {
  Logger logger = LoggerFactory.getLogger(ApiScraperService.class);
  @Autowired
  ApiPeerDataRepository peerRepo;
  @Autowired
  ApiPeerPointsDataRepository peerPointsRepo;
  @Autowired
  ApiCampusDataRepository campusRepo;
  @Autowired
  ApiRequestService requestService;

  public void getPeerFromCampus(ApiCampusData campus) {
    ApiParticipantLoginsDTO participantLogins = requestService.request(ApiParticipantLoginsDTO.class, "campuses/" + campus.getId() + "/participants?limit=1000&offset=0");
    for (String peerLogin : participantLogins.participants()) {

    }
  }


  public void updateCampuses() {
    logger.info("retrieving campus list...");
    campusRepo.saveAll(requestService.request(ApiCampusesDTO.class, "/campuses").campuses());
  }

  @Scheduled(fixedRateString = "PT1M")
  public void updatePeerList() {
    logger.info("updating peer info...");
    var peerList = StreamSupport.stream(peerRepo.findAll().spliterator(), true).toList();

    var changedPeerData = new ArrayList<ApiPeerData>();
    var changedPeerPointsData = new ArrayList<ApiPeerPointsData>();


    var peerFutures = new ArrayList<CompletableFuture<Void>>();
    try (ExecutorService rateLimitedExecutor = Executors.newFixedThreadPool(3)) {
      for (ApiPeerData peer : peerList) {
        CompletableFuture<Void> asd = CompletableFuture.supplyAsync(() -> {
          logger.info("peer " + peer.login());
          ApiPeerData peerResponse = requestService.request(ApiPeerData.class, "/participants/" + peer.login());
          ApiPeerPointsData peerPointsResponse = requestService.request(ApiPeerPointsData.class, "/participants/" + peer.login() + "/points");
          changedPeerData.add(peerResponse);
          changedPeerPointsData.add(peerPointsResponse);
          return null;
          }, rateLimitedExecutor);
        peerFutures.add(asd);
      }
      peerFutures.forEach(CompletableFuture::join);
    }
    if (!(changedPeerData.isEmpty() && changedPeerPointsData.isEmpty())) {
      peerRepo.saveAll(changedPeerData);
      peerPointsRepo.saveAll(changedPeerPointsData);
      logger.info("update finished");
    } else {
      logger.info("no peers were updated");
    }
  }
}
