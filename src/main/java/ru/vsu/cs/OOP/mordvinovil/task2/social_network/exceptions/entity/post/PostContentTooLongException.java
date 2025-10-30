package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.post;

public class PostContentTooLongException extends RuntimeException {
    public PostContentTooLongException(String message) {
        super(message);
    }
}
