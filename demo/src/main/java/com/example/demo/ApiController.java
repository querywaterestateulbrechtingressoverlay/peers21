package com.example.demo;

import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.data.Peer;
import com.example.demo.data.PeerBaseData;
import com.example.demo.data.PeerBaseDataRepository;
import com.example.demo.data.PeerMutableDataRepository;
import com.example.demo.scraper.ApiScraperService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
public class ApiController {

  @Autowired
  PeerBaseDataRepository baseDataRepo;

  @Autowired
  PeerMutableDataRepository mutableDataRepo;

  @Autowired
  ApiScraperService apiScraper;

  @GetMapping("/init")
  void initDatabase() {
    apiScraper.initPeerList();
  }

  @GetMapping("/peers")
  List<PeerBaseData> getPeers(@RequestParam(required = false) String wave,
                      @RequestParam(required = false) Integer tribe,
                      @RequestParam(defaultValue = "login") String orderBy,
                      @RequestParam(defaultValue = "50") int peersPerPage,
                      @RequestParam(defaultValue = "0") int page) {
    List<PeerBaseData> data;
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
}
