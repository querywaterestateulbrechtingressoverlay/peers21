package ru.cyphercola.peers21.datalayer.data.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cyphercola.peers21.datalayer.dto.PeerDataPaginatedDTO;
import ru.cyphercola.peers21.datalayer.dto.TribeDataDTOList;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/mockapi")
public class MockApiController {
  @Autowired
  MockTribeDataRepository mockTribeRepo;
  @Autowired
  MockPeerDataRepository mockPeerRepo;
  @GetMapping("/waves")
  List<String> getMockWaves() {
    return mockPeerRepo.findDistinctWaves();
  }
  @GetMapping("/tribes")
  TribeDataDTOList getMockTribes() {
    return new TribeDataDTOList(StreamSupport.stream(mockTribeRepo.findAll().spliterator(), false).map(MockTribeData::toDTO).toList());
  }
  @GetMapping("/peers")
  ResponseEntity<PeerDataPaginatedDTO> getMockPeers(@RequestParam(defaultValue = "login") String orderBy,
                                                    @RequestParam(defaultValue = "true") boolean orderAscending,
                                                    @RequestParam(defaultValue = "30") int peersPerPage,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(required = false) Integer tribeId,
                                                    @RequestParam(required = false) String wave) {
    Sort sort = Sort.by(((orderAscending) ? Sort.Direction.ASC : Sort.Direction.DESC), orderBy);
    Page<MockPeerData> peerData = switch (tribeId) {
      case null -> switch (wave) {
        case null -> mockPeerRepo.findAll(PageRequest.of(page, peersPerPage, sort));
        default -> mockPeerRepo.findByWave(wave, PageRequest.of(page, peersPerPage, sort));
      };
      default -> switch (wave) {
        case null -> mockPeerRepo.findByTribeId(tribeId, PageRequest.of(page, peersPerPage, sort));
        default -> mockPeerRepo.findByTribeIdAndWave(tribeId, wave, PageRequest.of(page, peersPerPage, sort));
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
      peerData.getContent().stream().map(MockPeerData::toDTO).toList(),
      page,
      peerData.getTotalPages(),
      peerData.hasPrevious() ? String.format(formatString, 0) : null,
      peerData.hasPrevious() ? String.format(formatString, page - 1) : null,
      peerData.hasNext() ? String.format(formatString, page + 1) : null,
      peerData.hasNext() ? String.format(formatString, peerData.getTotalPages() - 1) : null
    ), headers, HttpStatus.OK);
  }
}
