package ru.cyphercola.peers21.backend;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.cyphercola.peers21.backend.data.*;
import ru.cyphercola.peers21.backend.dto.PeerDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("api")
public class ApiController {

  @Autowired
  PeerDataRepository baseDataRepo;

  @GetMapping("/ping")
  String ping() {
    return "pong";
  }

  @GetMapping("/tribes")
  List<TribeData> getDistinctTribes() {
    return baseDataRepo.findDistinctTribes();
  }

  @GetMapping("/waves")
  List<String> getDistinctWaves() {
    return baseDataRepo.findDistinctWaves();
  }

  @GetMapping("/peers")
  ResponseEntity<PeerDataDTO> getPeers(@RequestParam(defaultValue = "login") String orderBy,
                                       @RequestParam(defaultValue = "true") boolean orderAscending,
                                       @RequestParam(defaultValue = "30") int peersPerPage,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(required = false) Integer tribeId,
                                       @RequestParam(required = false) String wave) {
    Sort sort = Sort.by(((orderAscending) ? Sort.Direction.ASC : Sort.Direction.DESC), orderBy);
    Page<PeerData> peerData = switch (tribeId) {
      case null -> switch (wave) {
        case null -> baseDataRepo.findAll(PageRequest.of(page, peersPerPage, sort));
        default -> baseDataRepo.findByWave(wave, PageRequest.of(page, peersPerPage, sort));
      };
      default -> switch (wave) {
        case null -> baseDataRepo.findByTribeId(tribeId, PageRequest.of(page, peersPerPage, sort));
        default -> baseDataRepo.findByTribeIdAndWave(tribeId, wave, PageRequest.of(page, peersPerPage, sort));
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

    return new ResponseEntity<>(new PeerDataDTO(peerData.getContent()), headers, HttpStatus.OK);
  }
  @PutMapping("/peer/{login}")
  void insertPeer(PeerDataDTO peerDataDTO) {
    baseDataRepo.upsert(peerDataDTO);
  }
}