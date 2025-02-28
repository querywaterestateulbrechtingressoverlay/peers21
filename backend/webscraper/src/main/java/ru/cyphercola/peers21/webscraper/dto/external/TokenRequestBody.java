package ru.cyphercola.peers21.webscraper.dto.external;

public class TokenRequestBody {
    String username;
    String password;
    String grant_type = "password";
    String client_id = "s21-open-api";

    public TokenRequestBody(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
