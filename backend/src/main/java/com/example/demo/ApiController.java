package com.example.demo;

import java.util.List;

import com.example.demo.data.PeerData;
import com.example.demo.data.PeerDataRepository;
import com.example.demo.scraper.ApiScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
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

  @CrossOrigin(allowedHeaders = {"Authorization"})
  @GetMapping("/peers")
  List<PeerData> getPeers(@RequestParam(defaultValue = "login") String orderBy,
                          @RequestParam(defaultValue = "50") int peersPerPage,
                          @RequestParam(defaultValue = "0") int page) {
    return baseDataRepo.findAll(PageRequest.of(page, peersPerPage, Sort.by(orderBy))).toList();
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
