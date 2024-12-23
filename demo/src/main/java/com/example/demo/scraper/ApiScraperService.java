package com.example.demo.scraper;

import com.example.demo.data.*;
import com.example.demo.scraper.dto.ParticipantDTO;
import com.example.demo.scraper.dto.CampusDTO;
import com.example.demo.scraper.dto.ParticipantLoginsDTO;
import com.example.demo.scraper.dto.ParticipantPointsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

  ApiRequestService requestService = ApiRequestService
      .getBuilder()
      .apiBaseUrl("https://edu-api.21-school.ru/services/21-school/api/v1/")
      .tokenEndpointUrl("https://auth.sberclass.ru/auth/realms/EduPowerKeycloak/protocol/openid-connect/token")
      .envUsernameVariable("API_USERNAME")
      .envPasswordVariable("API_PASSWORD")
      .rateLimit(2)
      .build();

  public void getPeerFromCampus(ApiCampusData campus) {
    logger.info(campus.getId());
    ParticipantLoginsDTO participantLogins = requestService.request(ParticipantLoginsDTO.class, "campuses/" + campus.getId() + "/participants?limit=20&offset=0");
    logger.info(String.valueOf(participantLogins.participants().size()));
    var participantDTOs = new ArrayList<ParticipantDTO>();
    for (String peerLogin : participantLogins.participants()) {
      logger.info(peerLogin);
      ParticipantDTO participant = requestService.request(ParticipantDTO.class, "participants/" + peerLogin);
      participantDTOs.add(participant);
    }
    Iterable<ApiPeerData> ids = peerRepo.saveAll(participantDTOs.stream().map(ParticipantDTO::toTableForm).toList());
    for (ApiPeerData a : ids) {
      peerPointsRepo.save(new ApiPeerPointsData(null, 0, 0, 0));
    }
  }



  public void updateCampuses() {
    logger.info("retrieving campus list...");
    campusRepo.saveAll(requestService.request(CampusDTO.class, "/campuses").campuses());
  }

  @Scheduled(fixedRateString = "PT1M")
  public void updatePeerList() {
    logger.info("updating peer info...");
    var peerList = StreamSupport.stream(peerRepo.findAll().spliterator(), true).toList();

    var changedPeerData = new ArrayList<ApiPeerData>();
    var changedPeerPointsData = new ArrayList<ApiPeerPointsData>();

    for (ApiPeerData peer : peerList) {
      logger.info("peer {}", peer.login());
      ParticipantDTO peerResponse;
      ParticipantPointsDTO peerPointsDTO;
      peerResponse = requestService.request(ParticipantDTO.class, "/participants/" + peer.login());
      peerPointsDTO = requestService.request(ParticipantPointsDTO.class, "/participants/" + peer.login() + "/points");
      changedPeerData.add(peerResponse.toTableForm(peer.id()));
      changedPeerPointsData.add(peerPointsDTO.toTableForm(peer.id()));
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
