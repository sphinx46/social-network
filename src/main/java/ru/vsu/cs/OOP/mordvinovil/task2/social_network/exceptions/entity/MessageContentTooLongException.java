package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class MessageContentTooLongException extends RuntimeException {
    public MessageContentTooLongException(String message) {
        super(message);
    }
}
