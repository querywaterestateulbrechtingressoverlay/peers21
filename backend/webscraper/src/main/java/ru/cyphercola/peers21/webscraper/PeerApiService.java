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
  ExternalApiRequestService extApiRequestService;
  @Autowired
  InternalApiRequestService intApiRequestService;


  public List<TribeDataDTO> getTribes(String campusId) {
    logger.debug("retrieving tribes from campus {}", campusId);
    List<TribeDataDTO> tribes = extApiRequestService
        .get(CoalitionsDTO.class, "/campuses/" + campusId + "/coalitions")
        .coalitions()
        .stream()
        .map((dto) -> new TribeDataDTO(dto.coalitionId(), dto.name()))
        .toList();
    logger.debug("retrieved {} tribes", tribes.size());
    return tribes;
  }

  public Map<String, Integer> getTribeParticipantLogins(TribeDataDTO tribe) {
    logger.debug("retrieving peer logins from tribe {}", tribe.name());
    List<String> participantLoginList = new ArrayList<>();
    int page = 0;
    while (true) {
    logger.trace("page {}", page);
      ParticipantLoginsDTO participantLogins = extApiRequestService
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
    logger.debug("retrieved {} peer logins", peers.size());
    return peers;
  }

  public void initPeerList() {
    String yktId = extApiRequestService.get(CampusesDTO.class, "/campuses").campuses().stream()
        .filter(campus -> campus.shortName().equals("21 Yakutsk"))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("couldn't find a campus with specified ID"))
        .id();
    logger.trace("ykt campus id = {}", yktId);
    List<TribeDataDTO> tribes = getTribes(yktId);
    logger.info("transferring tribe list to data layer...");
    intApiRequestService.put(new TribeDataDTOList(tribes), "/tribes");

    var peerTribes = new HashMap<String, Integer>();
    for (var tribe : tribes) {

      peerTribes.putAll(getTribeParticipantLogins(tribe));
    }
    var peers = new ArrayList<PeerDataDTO>();
    for (Map.Entry<String, Integer> peerLoginAndTribe : peerTribes.entrySet()) {
      ParticipantDTO peerDTO = extApiRequestService.get(ParticipantDTO.class, String.format("/participants/%s", peerLoginAndTribe.getKey()));
      if (Objects.equals(peerDTO.status(), "ACTIVE") || Objects.equals(peerDTO.status(), "FROZEN")) {
        logger.trace("saving peer {}...", peerLoginAndTribe.getKey());
        ParticipantPointsDTO peerPointsDTO = extApiRequestService.get(ParticipantPointsDTO.class, String.format("/participants/%s/points", peerLoginAndTribe.getKey()));
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
        logger.trace("peer {} is inactive, skipping", peerLoginAndTribe.getKey());
      }
    }
    intApiRequestService.put(new PeerDataDTOList(peers), "/peers");
    logger.info("application initialized");
  }

  PeerDataDTO updatePeer(PeerDataDTO peer) {
    logger.trace("updating peer {}", peer.login());
    ParticipantDTO peerDTO = extApiRequestService.get(ParticipantDTO.class, String.format("/participants/%s", peer.login()));
    ParticipantPointsDTO peerPointsDTO = extApiRequestService.get(ParticipantPointsDTO.class, String.format("/participants/%s/points", peer.login()));

    return new PeerDataDTO(
      peer.login(),
      peerDTO.className(),
      peer.tribeId(),
      peerDTO.status(),
      0,
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
    String apiUrl = "/peers?page=%d";
    PeerDataPaginatedDTO peerList;
    var changedPeerData = new ArrayList<PeerDataDTO>();
    do {
      peerList = intApiRequestService.get(PeerDataPaginatedDTO.class,String.format(apiUrl, page++));
      for (PeerDataDTO peer : peerList.peerData()) {
        changedPeerData.add(updatePeer(peer));
      }
    } while (peerList.nextPageUrl() != null);
    intApiRequestService.put(new PeerDataDTOList(changedPeerData), "/peers");
    logger.info("update finished, updated {} peers", changedPeerData.size());
  }
}
