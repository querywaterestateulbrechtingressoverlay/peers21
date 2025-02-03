package com.example.demo;

import com.example.demo.data.PeerDataRepository;
import com.example.demo.data.TribeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.Collectors;

@Controller
public class FrontendController {
  @Autowired
  private PeerDataRepository peerRepo;
  @GetMapping({"/", "peers"})
  String index(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "login") String orderBy, @RequestParam(defaultValue = "asc") String orderDir,  Model model) {
    Sort.Direction direction = orderDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort.Order order = new Sort.Order(direction, orderBy);
    var peerPage = peerRepo.findAll(PageRequest.of(page - 1, 30, Sort.by(order)));
    model.addAttribute("orderBy", orderBy);
    model.addAttribute("currentPage", peerPage.getNumber() + 1);
    model.addAttribute("totalPages", peerPage.getTotalPages());
    model.addAttribute("uniqueWaves", peerRepo.findDistinctWaves());
    model.addAttribute("uniqueTribes", peerRepo.findDistinctTribes().stream().collect(Collectors.toMap(TribeData::tribeId, TribeData::name)));
    model.addAttribute("sortDirection", orderDir);
    model.addAttribute("reverseSortDirection", orderDir.equals("asc") ? "desc" : "asc");
    model.addAttribute("peers", peerPage.getContent());
    return "index";
  }
}
