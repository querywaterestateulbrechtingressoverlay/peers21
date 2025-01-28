package com.example.demo;

import com.example.demo.data.Peer;
import com.example.demo.data.PeerBaseDataRepository;
import com.example.demo.data.PeerMutableDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.StreamSupport;

@Controller
public class FrontendController {
  @Autowired
  private PeerBaseDataRepository baseRepo;
  @Autowired
  private PeerMutableDataRepository mutableRepo;
  @GetMapping("/")
  String index(Model model) {
    model.addAttribute("users", StreamSupport.stream(baseRepo.findAll().spliterator(), false).map((p) -> Peer.combine(p, mutableRepo.findById(p.id()).get())));
    return "index";
  }
}
