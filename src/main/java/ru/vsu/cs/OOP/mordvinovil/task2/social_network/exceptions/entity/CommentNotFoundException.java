package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}