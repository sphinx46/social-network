package ru.cs.vsu.social_network.contents_service.exception.like;

public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(String message) {
        super(message);
    }
}
