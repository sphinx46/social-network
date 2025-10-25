package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class CommentContentTooLongException extends RuntimeException {
    public CommentContentTooLongException(String message) {
        super(message);
    }
}
