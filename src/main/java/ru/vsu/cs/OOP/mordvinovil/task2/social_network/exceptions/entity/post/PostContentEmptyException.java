package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.post;

public class PostContentEmptyException extends RuntimeException {
    public PostContentEmptyException(String message) {
        super(message);
    }
}
