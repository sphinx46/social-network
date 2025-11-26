package ru.cs.vsu.social_network.contents_service.exception;

public class PostNotFoundException extends RuntimeException {
  public PostNotFoundException(String message) {
    super(message);
  }
}
