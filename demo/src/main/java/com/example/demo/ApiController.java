package com.example.demo;

import com.example.demo.data.Peer;
import java.util.List;

import com.example.demo.data.PeerRepository;
import com.example.demo.data.PeerState;
import com.example.demo.scraper.ApiScraperService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
public class ApiController {

    @Autowired
    PeerRepository repo;

    @Autowired
    ApiScraperService apiScraper;

    @GetMapping("/")
    void updateApi() {
        repo.save(new Peer(null, "cypherco", PeerState.ALIVE, 2, 4, 9999, 9999, 9999, 9999));
        apiScraper.updateApiKey();
        apiScraper.updatePeerList();
    }

//    @GetMapping("/peers")
//    List<Peer> getPeers() {
//        return repo.getAllPeers();
//    }
//    @GetMapping("/peers/wave/{waveId}")
//    List<Peer> getPeersByWave(@PathVariable("waveId") @NotBlank int wave) {
//        return repo.findByWave(wave);
//    }
//
//    @GetMapping("/peers/{peerUsername}")
//    Peer getPeerById(@PathVariable @NotBlank String peerUsername) {
//        return repo.findByName(peerUsername);
//    }

}
