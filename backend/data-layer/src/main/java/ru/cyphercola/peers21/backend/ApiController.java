package ru.cyphercola.peers21.backend;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.cyphercola.peers21.backend.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.cyphercola.peers21.backend.dto.PeerDataDTO;

@RestController
@CrossOrigin
@RequestMapping("api")
public class ApiController {
  Logger logger = LoggerFactory.getLogger(ApiController.class);
  @Autowired
  PeerDataRepository peerRepo;

  @GetMapping("/ping")
  String ping() {
    return "pong";
  }

  @GetMapping("/tribes")
  List<TribeData> getDistinctTribes() {
    return peerRepo.findDistinctTribes();
  }

  @GetMapping("/waves")
  List<String> getDistinctWaves() {
    return peerRepo.findDistinctWaves();
  }

  @GetMapping("/peers")
  ResponseEntity<List<PeerDataDTO>> getPeers(@RequestParam(defaultValue = "login") String orderBy,
                                           @RequestParam(defaultValue = "true") boolean orderAscending,
                                           @RequestParam(defaultValue = "30") int peersPerPage,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(required = false) Integer tribeId,
                                           @RequestParam(required = false) String wave) {
    Sort sort = Sort.by(((orderAscending) ? Sort.Direction.ASC : Sort.Direction.DESC), orderBy);
    Page<PeerData> peerData = switch (tribeId) {
      case null -> switch (wave) {
        case null -> peerRepo.findAll(PageRequest.of(page, peersPerPage, sort));
        default -> peerRepo.findByWave(wave, PageRequest.of(page, peersPerPage, sort));
      };
      default -> switch (wave) {
        case null -> peerRepo.findByTribeId(tribeId, PageRequest.of(page, peersPerPage, sort));
        default -> peerRepo.findByTribeIdAndWave(tribeId, wave, PageRequest.of(page, peersPerPage, sort));
      };
    };
    String formatString = "/peers?orderBy=" + orderBy
        + "&orderAscending=" + orderAscending
        + "&peersPerPage=" + peersPerPage
        + "&page=%d"
        + ((tribeId == null) ? "" : ("&tribeId=" + tribeId))
        + ((wave == null) ? "" : ("&wave=" + wave));
    StringBuilder navigationLinks = new StringBuilder();
    if (peerData.hasPrevious()) {
      navigationLinks
          .append("<").append(String.format(formatString, 0)).append(">; rel=first,")
          .append("<").append(String.format(formatString, page - 1)).append(">; rel=previous");
    }
    if (peerData.hasNext()) {
      navigationLinks
          .append("<").append(String.format(formatString, page + 1)).append(">; rel=next,")
          .append("<").append(String.format(formatString, peerData.getTotalPages() - 1)).append(">; rel=last");
    }
    HttpHeaders headers = new HttpHeaders();
    headers.set("Link", navigationLinks.toString());

    return new ResponseEntity<>(peerData.getContent().stream().map(PeerData::toDTO).toList(), headers, HttpStatus.OK);
  }
  @PutMapping("/peer/{login}")
  void insertOrUpdatePeer(@PathVariable String login, @RequestBody PeerDataDTO peerDataDTO) {
    logger.info(peerDataDTO.login());
    logger.info(peerDataDTO.wave());
    logger.info(peerDataDTO.tribeId().toString());
    if (!Objects.equals(login, peerDataDTO.login())) {
      logger.error("lol");
    } else {
      Optional<PeerData> existingEntry = peerRepo.findFirst1ByLogin(peerDataDTO.login());
      peerRepo.save(peerDataDTO.toEntity(existingEntry.map(PeerData::id).orElse(null)));
    }
  }
}