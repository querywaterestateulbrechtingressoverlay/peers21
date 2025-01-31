package com.example.demo;

import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.data.Peer;
import com.example.demo.data.PeerBaseDataRepository;
import com.example.demo.data.PeerMutableDataRepository;
import com.example.demo.scraper.ApiScraperService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
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

//  @GetMapping("/campuses")
//  List<ApiCampusData> getCampuses() {
//    return StreamSupport.stream(campusRepo.findAll().spliterator(), false).toList();
//  }

//  @GetMapping("/peers")
//  List<Peer> getPeers(@RequestParam(required = false) int wave,
//                      @RequestParam(required = false) int intensive,
//                      @RequestParam(required = false) String tribe,
//                      @RequestParam(defaultValue = "0") int orderBy,
//                      @RequestParam(defaultValue = "50") int peersPerPage,
//                      @RequestParam(defaultValue = "0") int page) {
//    StreamSupport.stream(baseDataRepo.findAll().spliterator(), false)
//    return StreamSupport.stream(repo.findAll().spliterator(), false).toList();
//  }

//  @GetMapping("/peers/tribe/{tribe}")
//  List<Peer> getPeersFromTribe(@PathVariable String tribe,
//                               @RequestParam(defaultValue = "0") int orderBy,
//                               @RequestParam(defaultValue = "50") int peersPerPage,
//                               @RequestParam(defaultValue = "0") int page) {
//
//  }

//  @GetMapping("/peers/wave/{wave}")
//  List<Peer> getPeersFromWave(@PathVariable int wave,
//                              @RequestParam(defaultValue = "0") int orderBy,
//                              @RequestParam(defaultValue = "50") int peersPerPage,
//                              @RequestParam(defaultValue = "0") int page) {
//
//  }

//  @GetMapping("/peers/intensive/{intensive}")
//  List<Peer> getPeersFromIntensive(@PathVariable int intensive,
//                                   @RequestParam(defaultValue = "0") int orderBy,
//                                   @RequestParam(defaultValue = "50") int peersPerPage,
//                                   @RequestParam(defaultValue = "0") int page) {
//
//  }
//
//  @GetMapping("/peers/{peerUsername}")
//  PeerData getPeerById(@PathVariable @NotBlank String peerUsername) {
//    return repo.findByLogin(peerUsername);
//  }
}
