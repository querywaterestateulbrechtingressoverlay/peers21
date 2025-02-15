package ru.cyphercola.peers21.datalayer.security;

import ru.cyphercola.peers21.datalayer.data.ApiUserRepository;
import ru.cyphercola.peers21.datalayer.data.PeerDataRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;


public class CustomUserDetailsService implements UserDetailsService {
  @Autowired
  PeerDataRepository peerDataRepository;
  @Autowired
  ApiUserRepository apiUserRepository;
  PasswordEncoder encoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var apiUser = apiUserRepository.findFirst1ByLogin(username);
    LoggerFactory.getLogger(CustomUserDetailsService.class).info("loading user {} by username", username);
    LoggerFactory.getLogger(CustomUserDetailsService.class).info("asdf");
    LoggerFactory.getLogger(CustomUserDetailsService.class).info("asdf");
    LoggerFactory.getLogger(CustomUserDetailsService.class).info("id = " + apiUser.get().id().toString());
    if (apiUser.isPresent()) {
      LoggerFactory.getLogger(CustomUserDetailsService.class).info("api user");
      return User.builder()
        .username(apiUser.get().login())
        .password(encoder.encode(apiUser.get().password()))
        .authorities("ROLE_" + apiUser.get().role().toString())
        .build();
    } else {
      var peerData = peerDataRepository.findFirst1ByLogin(username);
      if (peerData.isPresent()) {
        LoggerFactory.getLogger(CustomUserDetailsService.class).info("peer user");
        return User.builder()
          .username(peerData.get().login())
          .password(encoder.encode("password"))
          .authorities("ROLE_USER")
          .build();
      } else {
        LoggerFactory.getLogger(CustomUserDetailsService.class).info("not found");
        throw new UsernameNotFoundException("user " + username + " not found");
      }
    }
  }
}
