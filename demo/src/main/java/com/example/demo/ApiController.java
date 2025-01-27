package com.example.demo;

import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.data.PeerBaseDataRepository;
import com.example.demo.data.PeerMutableDataRepository;
import com.example.demo.scraper.ApiScraperService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
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
//
//  @GetMapping("/campuses")
//  List<ApiCampusData> getCampuses() {
//    return StreamSupport.stream(campusRepo.findAll().spliterator(), false).toList();
//  }
//
//  @GetMapping("/peers")
//  List<PeerData> getPeers() {
//    return StreamSupport.stream(repo.findAll().spliterator(), false).toList();
//  }
//
//  @GetMapping("/peers/{peerUsername}")
//  PeerData getPeerById(@PathVariable @NotBlank String peerUsername) {
//    return repo.findByLogin(peerUsername);
//  }
}
