package ru.cyphercola.peers21.webscraper.exception;

public class ExternalServerErrorException extends RuntimeException {
  public ExternalServerErrorException(String message) {
    super(message);
  }
}
