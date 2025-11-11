package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(String message) {
        super(message);
    }
}
