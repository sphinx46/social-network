package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class RelationshipNoPendingRequestsException extends RuntimeException {
    public RelationshipNoPendingRequestsException(String message) {
        super(message);
    }
}
