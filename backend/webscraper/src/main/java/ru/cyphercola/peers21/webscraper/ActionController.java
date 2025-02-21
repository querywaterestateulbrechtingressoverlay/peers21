package ru.cyphercola.peers21.webscraper;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActionController {
  @Autowired
  PeerApiService pas;
  @GetMapping("/init")
  void init() {
    LoggerFactory.getLogger(ActionController.class).info("init");
    pas.initPeerList();
  }
}
