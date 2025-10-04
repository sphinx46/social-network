package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class RelationshipNotFoundException extends RuntimeException {
    public RelationshipNotFoundException(String message) {
        super(message);
    }
}
