package ru.cyphercola.peers21.datalayer.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cypherco.peersapp.datalayer.apiuser")
public record InitialApiUserCredentials(String username, String password) {
}
