package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
