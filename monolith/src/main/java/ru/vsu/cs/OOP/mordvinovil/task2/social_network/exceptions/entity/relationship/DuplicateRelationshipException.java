package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship;

public class DuplicateRelationshipException extends RuntimeException {
    public DuplicateRelationshipException(String message) {
        super(message);
    }
}
