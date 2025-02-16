package ru.cyphercola.peers21.webscraper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("cypherco.peersapp.api-request-service")
public record ApiRequestServiceProperties(String apiBaseUrl, String tokenEndpointUrl, String apiUsername, String apiPassword, int rateLimit) {}