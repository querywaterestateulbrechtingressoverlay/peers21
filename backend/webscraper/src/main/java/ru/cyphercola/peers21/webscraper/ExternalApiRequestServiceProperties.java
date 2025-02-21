package ru.cyphercola.peers21.webscraper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cypherco.peersapp.external-api-request-service")
public record ExternalApiRequestServiceProperties(String apiBaseUrl, String tokenEndpointUrl, String websiteUrl, String websiteAuthUrl, String apiUsername, String apiPassword, int rateLimit) {}