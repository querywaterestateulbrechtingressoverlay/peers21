package ru.cyphercola.peers21.webscraper;

import ru.cyphercola.peers21.webscraper.datalayerdto.*;
import ru.cyphercola.peers21.webscraper.dto.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@EnableScheduling
@Service
public class PeerApiService {
  Logger logger = LoggerFactory.getLogger(PeerApiService.class);
  @Autowired
  ExternalApiRequestService requestService;
  @Autowired
  InternalApiRequestService internalRequestService;
//  @Autowired
//  WebScraperService webScraper;

  public List<TribeDataDTO> getTribes(String campusId) {
    logger.info("retrieving tribes from campus {}", campusId);
    List<TribeDataDTO> tribes = requestService
        .get(CoalitionsDTO.class, "/campuses/" + campusId + "/coalitions")
        .coalitions()
        .stream()
        .map((dto) -> new TribeDataDTO(dto.coalitionId(), dto.name()))
        .toList();
    logger.info("retrieved {} tribes", tribes.size());
    return tribes;
  }

  public Map<String, Integer> getTribeParticipantLogins(TribeDataDTO tribe) {
    logger.info("retrieving peer logins from tribe {}", tribe.name());
    List<String> participantLoginList = new ArrayList<>();
    int page = 0;
    while (true) {
    logger.info("page {}", page);
      ParticipantLoginsDTO participantLogins = requestService
          .get(ParticipantLoginsDTO.class, "/coalitions/" + tribe.id() + "/participants?limit=50&offset=" + 50 * page++);
      if (participantLogins.participants().isEmpty()) {
        break;
      } else {
        participantLoginList.addAll(participantLogins.participants());
      }
    }
    var peers = new HashMap<String, Integer>();
    for (String login : participantLoginList) {
      peers.put(login, tribe.id());
    }
    logger.info("retrieved {} peer logins", peers.size());
    return peers;
  }

  public void initPeerList() {
    String yktId = requestService.get(CampusesDTO.class, "/campuses").campuses()
        .stream()
        .filter(campus -> campus.shortName().equals("21 Yakutsk"))
        .findFirst()
        .get()
        .id();
    logger.info("ykt campus id = {}", yktId);
    List<TribeDataDTO> tribes = getTribes(yktId);

    internalRequestService.put(new TribeDataDTOList(tribes), "/tribes");

    var peerTribes = new HashMap<String, Integer>();
    for (var tribe : tribes) {
      peerTribes.putAll(getTribeParticipantLogins(tribe));
    }
    var peers = new ArrayList<PeerDataDTO>();
    for (var peerLoginAndTribe : peerTribes.entrySet()) {
      ParticipantDTO peerDTO = requestService.get(ParticipantDTO.class, String.format("participants/%s", peerLoginAndTribe.getKey()));
      if (Objects.equals(peerDTO.status(), "ACTIVE") || Objects.equals(peerDTO.status(), "FROZEN")) {
        logger.info("saving peer {}...", peerLoginAndTribe.getKey());
        ParticipantPointsDTO peerPointsDTO = requestService.get(ParticipantPointsDTO.class, String.format("/participants/%s/points", peerLoginAndTribe.getKey()));
        peers.add(
          new PeerDataDTO(
            peerDTO.login(),
            peerDTO.className(),
            peerLoginAndTribe.getValue(),
            peerDTO.status(),
            0,
            peerDTO.expValue(),
            peerPointsDTO.peerReviewPoints(),
            peerPointsDTO.codeReviewPoints(),
            peerPointsDTO.coins()
          )
        );
      } else {
        logger.info("peer {} is inactive, skipping", peerLoginAndTribe.getKey());
      }
    }
    internalRequestService.put(new PeerDataDTOList(peers), "/peers");
    logger.info("application initialized");
  }

  PeerDataDTO updatePeer(PeerDataDTO peer) {
    logger.info("updating peer {}", peer.login());
    ParticipantDTO peerDTO = requestService.get(ParticipantDTO.class, String.format("/participants/%s", peer.login()));
    ParticipantPointsDTO peerPointsDTO = requestService.get(ParticipantPointsDTO.class, String.format("/participants/%s/points", peer.login()));

    int tribePoints = 0;

    return new PeerDataDTO(
      peer.login(),
      peerDTO.className(),
      peer.tribeId(),
      peerDTO.status(),
      tribePoints,
      peerDTO.expValue(),
      peerPointsDTO.peerReviewPoints(),
      peerPointsDTO.codeReviewPoints(),
      peerPointsDTO.coins()
    );
  }

  @Scheduled(fixedRateString = "PT15M")
  public void updatePeerList() {
    logger.info("updating peer info...");
    int page = 0;
    var peerList = internalRequestService.get(PeerDataPaginatedDTO.class,"/peers");
    var changedPeerData = new ArrayList<PeerDataDTO>();
    do {
      for (PeerDataDTO peer : peerList.peerData()) {
        changedPeerData.add(updatePeer(peer));
      }
    } while (peerList.nextPageUrl() != null);
    internalRequestService.put(new PeerDataDTOList(changedPeerData), "/peers");
    logger.info("update finished, updated {} peers", changedPeerData.size());
  }
}
