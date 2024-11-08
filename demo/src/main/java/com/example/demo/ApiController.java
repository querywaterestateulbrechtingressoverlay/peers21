package com.example.demo;

import com.example.demo.data.Peer;
import java.util.List;

import com.example.demo.data.PeerRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
public class ApiController {

    @Autowired
    PeerRepository repo;

    @GetMapping("/peers")
    List<Peer> getPeers() {
        return repo.getAllPeers();
    }
    @GetMapping("/peers/wave/{waveId}")
    List<Peer> getPeersByWave(@PathVariable("waveId") @NotBlank int wave) {
        return repo.findByWave(wave);
    }

    @GetMapping("/peers/{peerUsername}")
    Peer getPeerById(@PathVariable @NotBlank String peerUsername) {
        return repo.findByName(peerUsername);
    }

}
