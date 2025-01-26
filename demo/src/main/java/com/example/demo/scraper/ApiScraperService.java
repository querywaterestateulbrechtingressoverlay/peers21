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
import java.util.stream.StreamSupport;

@EnableScheduling
@Service
public class ApiScraperService {
  Logger logger = LoggerFactory.getLogger(ApiScraperService.class);
  @Autowired
  PeerDataRepository peerRepo;
  @Autowired
  ApiCampusDataRepository campusRepo;
  @Autowired
  ApiRequestService requestService;
  @Autowired
  IntensiveDatesRepository intensiveDatesRepository;
  public List<PeerData> getPeersFromCampus(ApiCampusData campus) {
    logger.info(campus.getId());
    var participantLoginList = new ArrayList<String>();
    int page = 0;
    while (true) {
      ParticipantLoginsDTO participantLogins = requestService.request(ParticipantLoginsDTO.class, "campuses/" + campus.getId() + "/participants?limit=50&offset=" + 50 * page++);
      if (participantLogins.participants().isEmpty()) {
        break;
      } else {
        participantLoginList.addAll(participantLogins.participants());
      }
    }
    var parsedPeerData = new ArrayList<PeerData>();
    for (String peerLogin : participantLoginList) {
      logger.info(peerLogin);
      ParticipantDTO participant = requestService.request(ParticipantDTO.class, "participants/" + peerLogin);
      if (participant.status() == PeerState.ACTIVE || participant.status() == PeerState.FROZEN) {
        ParticipantXpHistoryDTO xpHistory = requestService.request(ParticipantXpHistoryDTO.class, "participants/" + peerLogin + "/experience-history");
        Integer participantIntensive = 0;
        if (!xpHistory.expHistory().isEmpty()) {
          participantIntensive = intensiveDatesRepository.findIntensiveByFirstXpAccrualDate(xpHistory.expHistory().getLast().accrualDateTime());
        }
        parsedPeerData.add(PeerData.createFromDTO(participant, participantIntensive));
      }
    }
    return parsedPeerData;
  }

  public void initApplication() {
    campusRepo.saveAll(getCampuses());
    ApiCampusData ykt = campusRepo.findByShortName("21 Yakutsk");
    peerRepo.saveAll(getPeersFromCampus(ykt));
    logger.info("application initialized");
  }

  public List<ApiCampusData> getCampuses() {
    logger.info("retrieving campus list...");
    return requestService.request(CampusDTO.class, "/campuses").campuses();
  }

  public List<TribeData> getTribes(ApiCampusData campus) {
    return requestService
      .request(CoalitionsDTO.class, "/campuses/" + campus.getId() + "/coalitions")
      .coalitions()
      .stream()
      .map((dto) -> new TribeData(dto.coalitionId(), dto.name()))
      .toList();
  }

  public List<TribeParticipantData> getTribeParticipants(TribeData tribe) {
    return requestService
      .request(ParticipantLoginsDTO.class, "/coalitions/" + tribe.id() + "/participants")
      .participants()
      .stream()
      .map((peerLogin) -> new TribeParticipantData(null, tribe.id(), peerLogin))
      .toList();
  }

  @Scheduled(fixedRateString = "PT15M")
  public void updatePeerList() {
    logger.info("updating peer info...");
    var peerList = StreamSupport.stream(peerRepo.findAll().spliterator(), true).toList();
    var changedPeerData = new ArrayList<PeerData>();
    for (PeerData peer : peerList) {
      logger.info("peer {}", peer.login());
      ParticipantDTO peerResponse;
      ParticipantPointsDTO peerPointsDTO;
      peerResponse = requestService.request(ParticipantDTO.class, "/participants/" + peer.login());
      peerPointsDTO = requestService.request(ParticipantPointsDTO.class, "/participants/" + peer.login() + "/points");
      changedPeerData.add(PeerData.updateFromDTO(peer, peerResponse, peerPointsDTO));
    }
    peerRepo.saveAll(changedPeerData);
    logger.info("update finished, updated {} peers", changedPeerData.size());
  }
}
