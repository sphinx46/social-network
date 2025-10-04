package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class SelfRelationshipException extends RuntimeException {
    public SelfRelationshipException(String message) {
        super(message);
    }
}
