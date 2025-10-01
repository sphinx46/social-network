package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class ProfileAlreadyExistsException extends RuntimeException {
    public ProfileAlreadyExistsException(String message) {
        super(message);
    }
}