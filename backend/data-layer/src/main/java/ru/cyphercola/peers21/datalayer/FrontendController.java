package ru.cyphercola.peers21.datalayer;

import java.util.List;
import java.util.stream.StreamSupport;

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
import ru.cyphercola.peers21.datalayer.data.mock.MockPeerDataRepository;
import ru.cyphercola.peers21.datalayer.data.mock.MockTribeDataRepository;
import ru.cyphercola.peers21.datalayer.dto.*;

@RestController
@CrossOrigin
@RequestMapping("api")
public class FrontendController {
  @Autowired
  TribeDataRepository tribeRepo;
  @Autowired
  PeerDataRepository peerRepo;
  @Autowired
  MockTribeDataRepository mockTribeRepo;
  @Autowired
  MockPeerDataRepository mockPeerRepo;

  @GetMapping("/ping")
  String ping() {
    return "pong";
  }

  @GetMapping("/frontend/tribes")
  TribeDataDTOList getTribes() {
    return new TribeDataDTOList(StreamSupport.stream(tribeRepo.findAll().spliterator(), false).map(TribeData::toDTO).toList());
  }
  @DeleteMapping("/frontend/tribes")
  void deleteTribe(@RequestParam Integer tribeId) {
    tribeRepo.deleteById(
      tribeRepo
        .findFirst1ByTribeId(tribeId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tribe with id " + tribeId + " was not found"))
        .id());
  }
  @PutMapping("/frontend/tribes")
  void insertOrUpdateTribes(@RequestBody TribeDataDTOList tribeDataDTOs) {
    if (tribeDataDTOs.tribes() != null) {
      for (var tribeDataDTO : tribeDataDTOs.tribes()) {
        tribeRepo.save(tribeDataDTO.toEntity(tribeRepo
          .findFirst1ByTribeId(tribeDataDTO.id())
          .map(TribeData::id).orElse(null)));
      }
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/frontend/waves")
  List<String> getWaves() {
    return peerRepo.findDistinctWaves();
  }

  @GetMapping("/frontend/peers")
  ResponseEntity<PeerDataPaginatedDTO> getPeers(@RequestParam(defaultValue = "login") String orderBy,
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
    HttpHeaders headers = new HttpHeaders();
    String formatString = String.format("/peers?orderBy=%s&orderAscending=%s&peersPerPage=%d&page=%%d%s%s",
      orderBy,
      orderAscending,
      peersPerPage,
      (tribeId == null) ? "" : ("&tribeId=" + tribeId),
      (wave == null) ? "" : ("&wave=" + wave));
    if (peerData.hasPrevious()) {
      headers.add(HttpHeaders.LINK, "<" + String.format(formatString, 0) + ">; rel=first");
      headers.add(HttpHeaders.LINK, "<" + String.format(formatString, page - 1) + ">; rel=previous");
    }
    if (peerData.hasNext()) {
      headers.add(HttpHeaders.LINK, "<" + String.format(formatString, page + 1) + ">; rel=next");
      headers.add(HttpHeaders.LINK, "<" + String.format(formatString, peerData.getTotalPages() - 1) + ">; rel=last");
    }
    return new ResponseEntity<>(new PeerDataPaginatedDTO(
      peerData.getContent().stream().map(PeerData::toDTO).toList(),
      page,
      peerData.getTotalPages(),
      peerData.hasPrevious() ? String.format(formatString, 0) : null,
      peerData.hasPrevious() ? String.format(formatString, page - 1) : null,
      peerData.hasNext() ? String.format(formatString, page + 1) : null,
      peerData.hasNext() ? String.format(formatString, peerData.getTotalPages() - 1) : null
      ), headers, HttpStatus.OK);
  }
  @DeleteMapping("/frontend/peers")
  void deletePeer(@RequestParam String peerLogin) {
    peerRepo.deleteById(
      peerRepo
        .findFirst1ByLogin(peerLogin)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peer with login " + peerLogin + " was not found"))
        .id());
  }
  @PutMapping("/frontend/peers")
  void putPeers(@RequestBody PeerDataDTOList peerDataDTOs) {
    if (peerDataDTOs.peers() != null) {
      for (var peerDataDTO: peerDataDTOs.peers()) {
        peerRepo.save(peerDataDTO.toEntity(peerRepo
          .findFirst1ByLogin(peerDataDTO.login())
          .map(PeerData::id).orElse(null)));
      }
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
  }
}