package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class CommentEmptyContentException extends RuntimeException {
    public CommentEmptyContentException(String message) {
        super(message);
    }
}
