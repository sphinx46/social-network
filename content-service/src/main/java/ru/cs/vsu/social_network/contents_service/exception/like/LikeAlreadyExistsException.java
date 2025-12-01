package ru.cs.vsu.social_network.contents_service.exception.like;

public class LikeAlreadyExistsException extends RuntimeException {
    public LikeAlreadyExistsException(String message) {
        super(message);
    }
}
