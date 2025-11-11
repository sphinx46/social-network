package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
