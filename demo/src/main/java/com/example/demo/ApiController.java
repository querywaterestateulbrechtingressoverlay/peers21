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

  @GetMapping("/peers")
  List<PeerData> getPeers(@RequestParam(required = false) String wave,
                          @RequestParam(required = false) Integer tribe,
                          @RequestParam(defaultValue = "login") String orderBy,
                          @RequestParam(defaultValue = "50") int peersPerPage,
                          @RequestParam(defaultValue = "0") int page) {
    List<PeerData> data;
    if (wave != null) {
      if (tribe != null) {
        data = baseDataRepo.findByWaveAndTribeId(wave, tribe, PageRequest.of(page, peersPerPage, Sort.by(orderBy)));
      } else {
        data = baseDataRepo.findByWave(wave, PageRequest.of(page, peersPerPage, Sort.by(orderBy)));
      }
    } else if (tribe != null) {
      data = baseDataRepo.findByTribeId(tribe, PageRequest.of(page, peersPerPage, Sort.by(orderBy)));
    } else {
      data = baseDataRepo.findAll(PageRequest.of(page, peersPerPage, Sort.by(orderBy))).toList();
    }
    return data;
  }
  @GetMapping("/update")
  void updatePeerList() {
    apiScraper.updatePeerList();
  }
}
