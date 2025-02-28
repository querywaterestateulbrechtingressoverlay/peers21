package ru.cyphercola.peers21.datalayer.dto;

public record JWTokenDTO(
    String token,
    long expirySeconds
) {
}
