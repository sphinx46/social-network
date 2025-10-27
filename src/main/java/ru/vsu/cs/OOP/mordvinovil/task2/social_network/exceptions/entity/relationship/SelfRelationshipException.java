package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship;

public class SelfRelationshipException extends RuntimeException {
    public SelfRelationshipException(String message) {
        super(message);
    }
}
