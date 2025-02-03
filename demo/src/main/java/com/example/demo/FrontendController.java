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
  private PeerDataRepository baseRepo;
  @GetMapping({"/", "peers"})
  String index(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "login") String orderBy, @RequestParam(defaultValue = "asc") String orderDir,  Model model) {
    int totalPages = baseRepo.findAll(Pageable.ofSize(50)).getTotalPages();
    Sort.Direction direction = orderDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort.Order order = new Sort.Order(direction, orderBy);
    model.addAttribute("orderBy", orderBy);
    model.addAttribute("currentPage", 1);
    model.addAttribute("totalPages", totalPages);
    model.addAttribute("uniqueWaves", baseRepo.findDistinctWaves());
    model.addAttribute("uniqueTribes", baseRepo.findDistinctTribes().stream().collect(Collectors.toMap(TribeData::tribeId, TribeData::name)));
    model.addAttribute("peers", baseRepo.findAll(PageRequest.of(page, 50, Sort.by(order))).getContent());
    return "index";
  }
}
