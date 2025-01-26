package com.example.demo;

import com.example.demo.data.ApiCampusData;
import com.example.demo.data.ApiCampusDataRepository;
import com.example.demo.data.PeerData;
import com.example.demo.data.PeerDataRepository;
import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.scraper.ApiScraperService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
public class ApiController {

  @Autowired
  PeerDataRepository repo;

  @Autowired
  ApiScraperService apiScraper;
  @Autowired
  ApiCampusDataRepository campusRepo;

  @GetMapping("/init")
  void initDatabase() {
    apiScraper.initApplication();
  }

  @GetMapping("/campuses")
  List<ApiCampusData> getCampuses() {
    return StreamSupport.stream(campusRepo.findAll().spliterator(), false).toList();
  }

  @GetMapping("/peers")
  List<PeerData> getPeers() {
    return StreamSupport.stream(repo.findAll().spliterator(), false).toList();
  }

  @GetMapping("/peers/{peerUsername}")
  PeerData getPeerById(@PathVariable @NotBlank String peerUsername) {
    return repo.findByLogin(peerUsername);
  }
}
