package ru.cyphercola.peers21.webscraper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cypherco.peersapp.internal-api-request-service")
public record InternalApiRequestServiceProperties(String apiBaseUrl, String apiUsername, String apiPassword) {
}
