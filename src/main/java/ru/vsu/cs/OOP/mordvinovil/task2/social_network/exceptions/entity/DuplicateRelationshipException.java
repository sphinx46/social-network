package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class DuplicateRelationShipException extends RuntimeException {
  public DuplicateRelationShipException(String message) {
    super(message);
  }
}
