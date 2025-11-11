package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}