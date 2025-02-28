package ru.cyphercola.peers21.datalayer.security;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.log.LogMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.cyphercola.peers21.datalayer.data.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@EnableConfigurationProperties(InitialApiUserCredentials.class)
public class CustomUserDetailsService implements UserDetailsManager, InitializingBean {
  private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
  @Autowired
  private PeerDataRepository peerDataRepository;
  @Autowired
  private ApiUserRepository apiUserRepository;
  @Autowired
  private ApiUserAuthorityRepository authorityRepository;
  @Autowired
  private InitialApiUserCredentials initialCredentials;

  private final PasswordEncoder encoder  = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    logger.debug("loading user {}", username);
    var apiUser = apiUserRepository.findFirst1ByLogin(username);
    if (apiUser.isPresent()) {
      List<ApiUserAuthority> authorities = authorityRepository.findByApiUserLogin(username);
      return User.builder()
        .username(apiUser.get().login())
        .password(encoder.encode(apiUser.get().password()))
        .authorities(authorities.stream()
          .map(apiauth -> new SimpleGrantedAuthority(apiauth.authority()))
          .toList())
        .build();
    } else {
      logger.debug("user {} not found in the api user database", username);
      var peerData = peerDataRepository.findFirst1ByLogin(username);
      if (peerData.isPresent()) {
        return User.builder()
          .username(peerData.get().login())
          .password(encoder.encode("password"))
          .authorities("user")
          .build();
      } else {
        throw new UsernameNotFoundException("user " + username + " not found");
      }
    }
  }

  @Override
  public void createUser(UserDetails user) {
    apiUserRepository.save(new ApiUser(null, user.getUsername(), user.getPassword()));
    user.getAuthorities().forEach(authority -> authorityRepository.save(new ApiUserAuthority(null, user.getUsername(), authority.getAuthority())));
  }

  @Override
  public void updateUser(UserDetails user) {
    apiUserRepository.findFirst1ByLogin(user.getUsername()).ifPresentOrElse(savedUser -> {
      apiUserRepository.save(new ApiUser(savedUser.id(), user.getUsername(), user.getPassword()));
      ArrayList<String> newAuthorities = new ArrayList<>(user.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .toList()
      );
      authorityRepository.findByApiUserLogin(user.getUsername()).forEach(savedAuth -> {
        if (!newAuthorities.contains(savedAuth.authority())) {
          authorityRepository.deleteById(savedAuth.id());
        } else {
          newAuthorities.remove(savedAuth.authority());
        }
      });
      newAuthorities.forEach(auth -> authorityRepository.save(new ApiUserAuthority(null, user.getUsername(), auth)));
    }, () -> {
      throw new UsernameNotFoundException("error while updating: username " + user.getUsername() + " not found");
    });
  }

  @Override
  public void deleteUser(String username) {
    apiUserRepository.findFirst1ByLogin(username).ifPresentOrElse(savedUser -> {
      apiUserRepository.deleteById(savedUser.id());
      authorityRepository.findByApiUserLogin(username).forEach(auth -> authorityRepository.deleteById(auth.id()));
    }, () -> {
      throw new UsernameNotFoundException("error while deleting: username " + username + " not found");
    });
  }

  @Override
  public void changePassword(String oldPassword, String newPassword) {
  }

  @Override
  public boolean userExists(String username) {
    return apiUserRepository.findFirst1ByLogin(username).isPresent() || peerDataRepository.findFirst1ByLogin(username).isPresent();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (!userExists(initialCredentials.username())) {
      logger.info("creating a initial user {} with password {}", initialCredentials.username(), initialCredentials.password());
      createUser(new User(initialCredentials.username(), initialCredentials.password(), List.of(new SimpleGrantedAuthority("api"), new SimpleGrantedAuthority("admin"), new SimpleGrantedAuthority("user"))));
    }
  }
}
