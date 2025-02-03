package com.example.demo.scraper;

import com.example.demo.data.*;
import com.example.demo.scraper.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@EnableScheduling
@Service
public class ApiScraperService {
  Logger logger = LoggerFactory.getLogger(ApiScraperService.class);
  @Autowired
  IntensiveDataRepository intensiveDataRepo;
  @Autowired
  TribeDataRepository tribeDataRepo;
  @Autowired
  PeerDataRepository peerRepo;
  @Autowired
  ApiRequestService requestService;

  public List<TribeData> getTribes(String campusId) {
    logger.info("retrieving tribes from campus {}", campusId);
    List<TribeData> tribes = requestService
        .request(CoalitionsDTO.class, "/campuses/" + campusId + "/coalitions")
        .coalitions()
        .stream()
        .map((dto) -> new TribeData(null, dto.coalitionId(), dto.name()))
        .toList();
    logger.info("retrieved {} tribes", tribes.size());
    return tribes;
  }

  public Map<String, Integer> getTribeParticipantLogins(TribeData tribe) {
    logger.info("retrieving peer logins from tribe {}", tribe.name());
    List<String> participantLoginList = new ArrayList<>();
    int page = 0;
    while (true) {
    logger.info("page {}", page);
      ParticipantLoginsDTO participantLogins = requestService
          .request(ParticipantLoginsDTO.class, "/coalitions/" + tribe.tribeId() + "/participants?limit=50&offset=" + 50 * page++);
      if (participantLogins.participants().isEmpty()) {
        break;
      } else {
        participantLoginList.addAll(participantLogins.participants());
      }
    }
    var peers = new HashMap<String, Integer>();
    for (String login : participantLoginList) {
      peers.put(login, tribe.tribeId());
    }
    logger.info("retrieved {} peer logins", peers.size());
    return peers;
  }

  public int getPeerIntensive(String peerLogin) {
    ParticipantXpHistoryDTO xpHistory = requestService
        .request(ParticipantXpHistoryDTO.class, "participants/" + peerLogin + "/experience-history");
    int participantIntensive = 0;
    if (!xpHistory.expHistory().isEmpty()) {
      participantIntensive = intensiveDataRepo
          .findIntensiveByFirstXpAccrualDate(xpHistory.expHistory().getLast().accrualDateTime());
    }
    return participantIntensive;
  }

  public void initPeerList() {
    String yktId = requestService.request(CampusesDTO.class, "/campuses").campuses()
        .stream()
        .filter(campus -> campus.shortName().equals("21 Yakutsk"))
        .findFirst()
        .get()
        .id();
    logger.info("ykt campus id = {}", yktId);
    List<TribeData> tribes = getTribes(yktId);
    tribeDataRepo.saveAll(tribes);
    var peerTribes = new HashMap<String, Integer>();
    for (var tribe : tribes) {
      peerTribes.putAll(getTribeParticipantLogins(tribe));
    }
    for (var peerLoginAndTribe : peerTribes.entrySet()) {
      ParticipantDTO peerDTO = requestService.request(ParticipantDTO.class, String.format("participants/%s", peerLoginAndTribe.getKey()));
      if (peerDTO.status() == PeerState.ACTIVE || peerDTO.status() == PeerState.FROZEN) {
        logger.info("saving peer {}...", peerLoginAndTribe.getKey());
        int intensive = getPeerIntensive(peerLoginAndTribe.getKey());
        ParticipantPointsDTO peerPointsDTO = requestService.request(ParticipantPointsDTO.class, String.format("/participants/%s/points", peerLoginAndTribe.getKey()));
        peerRepo.save(
          PeerData.createFromDTO(peerDTO, peerPointsDTO, intensive, peerLoginAndTribe.getValue(), 0)
        );
      } else {
        logger.info("peer {} is inactive, skipping", peerLoginAndTribe.getKey());
      }
    }
    logger.info("application initialized");
  }

  PeerData updatePeer(PeerData peer) {
    logger.info("updating peer {}", peer.login());
    ParticipantDTO peerDTO = requestService.request(ParticipantDTO.class, String.format("/participants/%s", peer.login()));
    ParticipantPointsDTO peerPointsDTO = requestService.request(ParticipantPointsDTO.class, String.format("/participants/%s/points", peer.login()));
    return peer.updateFromDTO(peerDTO, peerPointsDTO, 0);
  }

  @Scheduled(fixedRateString = "PT10M")
  public void updatePeerList() {
    logger.info("updating peer info...");
    var peerList = peerRepo.findAll();
    var changedPeerData = new ArrayList<PeerData>();
    for (PeerData peer : peerList) {
      changedPeerData.add(updatePeer(peer));
    }
    peerRepo.saveAll(changedPeerData);
    logger.info("update finished, updated {} peers", changedPeerData.size());
  }
}
