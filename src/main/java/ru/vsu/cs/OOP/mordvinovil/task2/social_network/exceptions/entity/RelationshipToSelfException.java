package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class RelationshipToSelfException extends RuntimeException {
  public RelationshipToSelfException(String message) {
    super(message);
  }
}
