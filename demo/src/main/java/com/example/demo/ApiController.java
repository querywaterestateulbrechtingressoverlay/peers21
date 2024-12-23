package com.example.demo;

import com.example.demo.data.ApiCampusData;
import com.example.demo.data.ApiCampusDataRepository;
import com.example.demo.data.ApiPeerData;
import com.example.demo.data.ApiPeerDataRepository;
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
    ApiPeerDataRepository repo;

    @Autowired
    ApiScraperService apiScraper;
    @Autowired
    ApiCampusDataRepository campusRepo;

    @GetMapping("/")
    void updateApi() {
        apiScraper.updateCampuses();
        ApiCampusData yakutsk = campusRepo.findByShortName("21 Yakutsk");
        apiScraper.getPeerFromCampus(yakutsk);
//        apiScraper.updatePeerList();
    }

    @GetMapping("/campuses")
    List<ApiCampusData> getCampuses() {
        return StreamSupport.stream(campusRepo.findAll().spliterator(), false).toList();
    }

    @GetMapping("/peers")
    List<ApiPeerData> getPeers() {
        return StreamSupport.stream(repo.findAll().spliterator(), false).toList();
    }
//    @GetMapping("/peers/wave/{waveId}")
//    List<ApiPeerData> getPeersByWave(@PathVariable("waveId") @NotBlank int wave) {
//        return repo.findByWave(wave);
//    }

    @GetMapping("/peers/{peerUsername}")
    ApiPeerData getPeerById(@PathVariable @NotBlank String peerUsername) {
        return repo.findByLogin(peerUsername);
    }

}
