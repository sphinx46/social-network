package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(String message) {
        super(message);
    }
}
