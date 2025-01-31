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
  PeerBaseDataRepository peerBaseDataRepo;
  @Autowired
  PeerMutableDataRepository peerMutableDataRepo;
  @Autowired
  ApiRequestService requestService;

  public List<ParticipantDTO> getPeersFromCampus(String campusId) {
    var participantLoginList = new ArrayList<String>();
    int page = 0;
    while (true) {
      ParticipantLoginsDTO participantLogins = requestService.request(ParticipantLoginsDTO.class, "campuses/" + campusId + "/participants?limit=50&offset=" + 50 * page++);
      if (participantLogins.participants().isEmpty()) {
        break;
      } else {
        participantLoginList.addAll(participantLogins.participants());
      }
    }
    var parsedPeerData = new ArrayList<ParticipantDTO>();
    for (String peerLogin : participantLoginList) {
      logger.info(peerLogin);
      ParticipantDTO participant = requestService.request(ParticipantDTO.class, "participants/" + peerLogin);
      if (participant.status() == PeerState.ACTIVE || participant.status() == PeerState.FROZEN) {
        parsedPeerData.add(participant);
      }
    }
    return parsedPeerData;
  }

  public List<TribeData> getTribes(String campusId) {
    return requestService
        .request(CoalitionsDTO.class, "/campuses/" + campusId + "/coalitions")
        .coalitions()
        .stream()
        .map((dto) -> new TribeData(null, dto.coalitionId(), dto.name()))
        .toList();
  }

  public Map<String, Integer> getTribeParticipantLogins(TribeData tribe) {
    List<String> participantLoginList = new ArrayList<>();
    int page = 0;
//    while (true) {
      ParticipantLoginsDTO participantLogins = requestService
          .request(ParticipantLoginsDTO.class, "/coalitions/" + tribe.tribeId() + "/participants?limit=5&offset=" + 50 * page++);
//      if (participantLogins.participants().isEmpty()) {
//        break;
//      } else {
        participantLoginList.addAll(participantLogins.participants());
//      }
//    }
    var asd = new HashMap<String, Integer>();
    participantLoginList.forEach(l -> {
      asd.put(l, tribe.tribeId());
    });
    return asd;
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
//    List<ParticipantDTO> peerData = getPeersFromCampus(yktId);
    List<TribeData> tribes = getTribes(yktId);
    tribeDataRepo.saveAll(tribes);
    var peerTribes = new HashMap<String, Integer>();
    for (TribeData t : tribes) {
      peerTribes.putAll(getTribeParticipantLogins(t));
    }
    for (var a : peerTribes.entrySet()) {
      logger.info(a.getKey());
      ParticipantDTO peer = requestService.request(ParticipantDTO.class, "participants/" + a.getKey());
      if (peer.status() == PeerState.ACTIVE || peer.status() == PeerState.FROZEN) {
        int intensive = getPeerIntensive(a.getKey());
        peerBaseDataRepo.save(
            new PeerBaseData(
                null, peer.login(), peer.className(), intensive, a.getValue(), null, null
            )
        );
      }

    }
    logger.info("application initialized");
  }

  @Scheduled(fixedRateString = "PT10M")
  public void updatePeerList() {
    logger.info("updating peer info...");
    var peerList = peerBaseDataRepo.findAll();
    var changedPeerData = new ArrayList<PeerMutableData>();
    for (PeerBaseData peer : peerList) {
      logger.info("peer {}", peer.login());
      ParticipantDTO peerResponse = requestService.request(ParticipantDTO.class, "/participants/" + peer.login());
      ParticipantPointsDTO peerPointsDTO = requestService.request(ParticipantPointsDTO.class, "/participants/" + peer.login() + "/points");
      changedPeerData.add(PeerMutableData.updateFromDTO(
        peer.peerMutableData() == null ? null : peer.peerMutableData().id(),
        peer.id(),
        0, peerResponse, peerPointsDTO));
    }
    peerMutableDataRepo.saveAll(changedPeerData);
    logger.info("update finished, updated {} peers", changedPeerData.size());
  }
}
