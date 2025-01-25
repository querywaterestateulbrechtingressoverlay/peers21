package com.example.demo;

import com.example.demo.data.PeerDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {
  @Autowired
  private PeerDataRepository peerRepo;
  @GetMapping("/")
  String index(Model model) {
    model.addAttribute("users", peerRepo.findAll());
    return "index";
  }
}
