package ru.cyphercola.peers21.backend;

import java.util.List;

import org.springframework.data.domain.Page;
import ru.cyphercola.peers21.backend.data.*;
import ru.cyphercola.peers21.backend.dto.PeerDataDTO;
import ru.cyphercola.peers21.backend.scraper.ApiScraperService;
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

  @Autowired
  ApiScraperService apiScraper;

  @GetMapping("/init")
  void initDatabase() {
    apiScraper.initPeerList();
  }

  @GetMapping("/tribes")
  List<TribeData> getDistinctTribes() {
    return baseDataRepo.findDistinctTribes();
  }

  @GetMapping("/waves")
  List<String> getDistinctWaves() {
    return baseDataRepo.findDistinctWaves();
  }

  @CrossOrigin
  @GetMapping("/peers")
  PeerDataDTO getPeers(@RequestParam(defaultValue = "login") String orderBy,
                       @RequestParam(defaultValue = "true") boolean orderAscending,
                       @RequestParam(defaultValue = "30") int peersPerPage,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) Integer tribeId,
                       @RequestParam(required = false) String wave) {
    Sort sort = Sort.by(((orderAscending) ? Sort.Direction.ASC : Sort.Direction.DESC), orderBy);
    Page<PeerData> peerData;
    if (tribeId != null && wave == null) {
      peerData = baseDataRepo.findByTribeId(tribeId, PageRequest.of(page, peersPerPage, sort));
    } else if (tribeId == null && wave != null) {
      peerData = baseDataRepo.findByWave(wave, PageRequest.of(page, peersPerPage, sort));
    } else if (tribeId != null) {
      peerData = baseDataRepo.findByTribeIdAndWave(tribeId, wave, PageRequest.of(page, peersPerPage, sort));
    } else {
      peerData = baseDataRepo.findAll(PageRequest.of(page, peersPerPage, sort));
    }
    return new PeerDataDTO(peerData.getContent(), page, peerData.getTotalPages());
  }
  @GetMapping("/update")
  void updatePeerList() {
    apiScraper.updatePeerList();
  }
}

// String index(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "login") String orderBy, @RequestParam(defaultValue = "asc") String orderDir,  Model model) {
//     Sort.Direction direction = orderDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
//     Sort.Order order = new Sort.Order(direction, orderBy);
//     var peerPage = peerRepo.findAll(PageRequest.of(page - 1, 30, Sort.by(order)));
//     model.addAttribute("orderBy", orderBy);
//     model.addAttribute("currentPage", peerPage.getNumber() + 1);
//     model.addAttribute("totalPages", peerPage.getTotalPages());
//     model.addAttribute("uniqueWaves", peerRepo.findDistinctWaves());
//     model.addAttribute("uniqueTribes", peerRepo.findDistinctTribes().stream().collect(Collectors.toMap(TribeData::tribeId, TribeData::name)));
//     model.addAttribute("sortDirection", orderDir);
//     model.addAttribute("reverseSortDirection", orderDir.equals("asc") ? "desc" : "asc");
//     model.addAttribute("peers", peerPage.getContent());
//     model.addAttribute("loggedIn", true);
//     return "index";
