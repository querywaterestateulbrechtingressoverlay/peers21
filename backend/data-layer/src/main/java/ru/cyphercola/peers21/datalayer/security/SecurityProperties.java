package ru.cyphercola.peers21.datalayer.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties("cypherco.peersapp.datalayer.security")
public record SecurityProperties(String apiUsername, String apiPassword, RSAPrivateKey rsaPrivate, RSAPublicKey rsaPublic) {
}
