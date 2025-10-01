package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message) {
        super(message);
    }
}