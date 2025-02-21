package ru.cyphercola.peers21.datalayer.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cypherco.peersapp.data-layer.api-user")
public record InitialApiUserCredentials(String username, String password) {
}
