package ru.cyphercola.peers21.webscraper;

import org.springframework.web.client.RestClient;
import ru.cyphercola.peers21.webscraper.datalayerdto.PeerDataDTO;
import ru.cyphercola.peers21.webscraper.datalayerdto.PeerDataDTOList;
import ru.cyphercola.peers21.webscraper.datalayerdto.TribeDataDTO;
import ru.cyphercola.peers21.webscraper.datalayerdto.TribeDataDTOList;
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
public class ApiScraperService {
  Logger logger = LoggerFactory.getLogger(ApiScraperService.class);
  @Autowired
  ApiRequestService requestService;

  String dataLayerApiURI = "/api";
  String dataLayerApiUsername = "admin";
  String dataLayerApiPassword = "adminpassword";
  @Autowired
  RestClient restClient;

  void updateTribes(List<TribeDataDTO> tribes) {
    restClient.put()
      .uri(dataLayerApiURI + "/tribes")
      .body(new TribeDataDTOList(tribes))
      .header("Authorization", "Basic " + Base64
        .getEncoder()
        .encodeToString((dataLayerApiUsername + ":" + dataLayerApiPassword).getBytes()))
      .body(Void.class);
  }

  void updatePeers(List<PeerDataDTO> peers) {
    restClient.put()
      .uri(dataLayerApiURI + "/peers")
      .body(new PeerDataDTOList(peers))
      .header("Authorization", "Basic " +
        Base64
          .getEncoder()
          .encodeToString((dataLayerApiUsername + ":" + dataLayerApiPassword).getBytes()))
      .body(Void.class);
  }

  public List<TribeDataDTO> getTribes(String campusId) {
    logger.info("retrieving tribes from campus {}", campusId);
    List<TribeDataDTO> tribes = requestService
        .request(CoalitionsDTO.class, "/campuses/" + campusId + "/coalitions")
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
          .request(ParticipantLoginsDTO.class, "/coalitions/" + tribe.id() + "/participants?limit=50&offset=" + 50 * page++);
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
    String yktId = requestService.request(CampusesDTO.class, "/campuses").campuses()
        .stream()
        .filter(campus -> campus.shortName().equals("21 Yakutsk"))
        .findFirst()
        .get()
        .id();
    logger.info("ykt campus id = {}", yktId);
    List<TribeDataDTO> tribes = getTribes(yktId);
    updateTribes(tribes);
    var peerTribes = new HashMap<String, Integer>();
    for (var tribe : tribes) {
      peerTribes.putAll(getTribeParticipantLogins(tribe));
    }
    var peers = new ArrayList<PeerDataDTO>();
    for (var peerLoginAndTribe : peerTribes.entrySet()) {
      ParticipantDTO peerDTO = requestService.request(ParticipantDTO.class, String.format("participants/%s", peerLoginAndTribe.getKey()));
      if (Objects.equals(peerDTO.status(), "ACTIVE") || Objects.equals(peerDTO.status(), "FROZEN")) {
        logger.info("saving peer {}...", peerLoginAndTribe.getKey());
        ParticipantPointsDTO peerPointsDTO = requestService.request(ParticipantPointsDTO.class, String.format("/participants/%s/points", peerLoginAndTribe.getKey()));
        peers.add(
          new PeerDataDTO(
            peerDTO.login(),
            peerDTO.parallelName(),
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
    updatePeers(peers);
    logger.info("application initialized");
  }

  PeerDataDTO updatePeer(PeerDataDTO peer) {
    logger.info("updating peer {}", peer.login());
    ParticipantDTO peerDTO = requestService.request(ParticipantDTO.class, String.format("/participants/%s", peer.login()));
    ParticipantPointsDTO peerPointsDTO = requestService.request(ParticipantPointsDTO.class, String.format("/participants/%s/points", peer.login()));
    int tribePoints = webCrawler.getTribePoints(peer.login());
    return new PeerDataDTO(
      peer.login(),
      peerDTO.parallelName(),
      peer.tribeId(),
      peerDTO.status(),
      tribePoints,
      peerDTO.expValue(),
      peerPointsDTO.peerReviewPoints(),
      peerPointsDTO.codeReviewPoints(),
      peerPointsDTO.coins()
    );
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
