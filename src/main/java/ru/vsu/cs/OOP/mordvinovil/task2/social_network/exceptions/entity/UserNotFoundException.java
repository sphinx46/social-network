package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("Пользователь с ID " + userId + " не найден");
    }
}