package ru.cyphercola.peers21.webscraper.dto.internal;

public record JWTokenDTO(
    String token,
    long expirySeconds
) {
}
