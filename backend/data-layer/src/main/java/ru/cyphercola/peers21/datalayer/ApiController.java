package ru.cyphercola.peers21.datalayer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import ru.cyphercola.peers21.datalayer.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.cyphercola.peers21.datalayer.dto.PeerDataDTO;
import ru.cyphercola.peers21.datalayer.dto.TribeDataDTO;

@RestController
@CrossOrigin
@RequestMapping("api")
public class ApiController {
  @Autowired
  TribeDataRepository tribeRepo;
  @Autowired
  PeerDataRepository peerRepo;

  @GetMapping("/ping")
  String ping() {
    return "pong";
  }

  @GetMapping("/tribes")
  Iterable<TribeData> getTribes() {
    return tribeRepo.findAll();
  }
  @DeleteMapping("/tribes")
  void deleteTribe(@RequestParam Integer tribeId) {
    tribeRepo.deleteById(
      tribeRepo
        .findFirst1ByTribeId(tribeId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tribe with id " + tribeId + " was not found"))
        .id());
  }
  @PutMapping("/tribes")
  void insertOrUpdateTribes(@RequestBody List<TribeDataDTO> tribeDataDTOS) {
    for (var tribeDataDTO: tribeDataDTOS) {
      tribeRepo.save(tribeDataDTO.toEntity(tribeRepo
        .findFirst1ByTribeId(tribeDataDTO.id())
        .map(TribeData::id).orElse(null)));
    }
  }

  @GetMapping("/waves")
  List<String> getWaves() {
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
  @DeleteMapping("/peers")
  void deletePeer(@RequestParam String peerLogin) {
    peerRepo.deleteById(
      peerRepo
        .findFirst1ByLogin(peerLogin)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peer with login " + peerLogin + " was not found"))
        .id());
  }
  @PutMapping("/peers")
  void putPeers(@RequestBody List<PeerDataDTO> peerDataDTOS) {
    for (var peerDataDTO: peerDataDTOS) {
      peerRepo.save(peerDataDTO.toEntity(peerRepo
        .findFirst1ByLogin(peerDataDTO.login())
        .map(PeerData::id).orElse(null)));
    }
  }
}