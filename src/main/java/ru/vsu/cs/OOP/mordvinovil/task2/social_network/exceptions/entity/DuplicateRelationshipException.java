package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class DuplicateRelationshipException extends RuntimeException {
    public DuplicateRelationshipException(String message) {
        super(message);
    }
}
