package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message;

public class MessageContentEmptyException extends RuntimeException {
    public MessageContentEmptyException(String message) {
        super(message);
    }
}
