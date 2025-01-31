package com.example.demo;

import com.example.demo.data.Peer;
import com.example.demo.data.PeerBaseData;
import com.example.demo.data.PeerBaseDataRepository;
import com.example.demo.data.PeerMutableDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.StreamSupport;

@Controller
public class FrontendController {
  @Autowired
  private PeerBaseDataRepository baseRepo;
  @Autowired
  private PeerMutableDataRepository mutableRepo;
  @GetMapping("/")
  String index(@RequestParam(defaultValue = "1") int page, Model model) {
    int totalPages = baseRepo.findAll(Pageable.ofSize(50)).getTotalPages();
    model.addAttribute("currentPage", 1);
    model.addAttribute("totalPages", totalPages);
    model.addAttribute("uniqueWaves", baseRepo.findDistinctWaves());
    model.addAttribute("uniqueTribes", baseRepo.findDistinctTribes());
    model.addAttribute("peers", StreamSupport.stream(baseRepo.findAll().spliterator(), false).toList());
    return "index";
  }
}
