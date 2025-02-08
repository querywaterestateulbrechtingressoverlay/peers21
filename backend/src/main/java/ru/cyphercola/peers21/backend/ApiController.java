package ru.cyphercola.peers21.backend;

import java.util.List;

import ru.cyphercola.peers21.backend.data.*;
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
  List<PeerData> getPeers(@RequestParam(defaultValue = "login") String orderBy,
                          @RequestParam(defaultValue = "true") boolean orderAscending,
                          @RequestParam(defaultValue = "50") int peersPerPage,
                          @RequestParam(defaultValue = "0") int page) {
    Sort sort = Sort.by(((orderAscending) ? Sort.Direction.ASC : Sort.Direction.DESC), orderBy);
    return baseDataRepo.findAll(PageRequest.of(page, peersPerPage, sort)).toList();
  }
  @GetMapping("/wave/{waveId}")
  List<PeerData> getPeersByWave(
    @PathVariable Integer waveId,
    @RequestParam(defaultValue = "login") String orderBy,
    @RequestParam(defaultValue = "50") int peersPerPage,
    @RequestParam(defaultValue = "0") int page
  ) {
    return baseDataRepo.findByWave(waveId, PageRequest.of(page, peersPerPage, Sort.by(orderBy)));
  }
  @GetMapping("/tribe/{tribeId}")
  List<PeerData> getPeersByTribe(
    @PathVariable Integer tribeId,
    @RequestParam(defaultValue = "login") String orderBy,
    @RequestParam(defaultValue = "50") int peersPerPage,
    @RequestParam(defaultValue = "0") int page
  ) {
    return baseDataRepo.findByTribeId(tribeId, PageRequest.of(page, peersPerPage, Sort.by(orderBy)));
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
