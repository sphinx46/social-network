package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.like;

public class LikeAlreadyExistsException extends RuntimeException {
    public LikeAlreadyExistsException(String message) {
        super(message);
    }
}
