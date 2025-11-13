package ru.cs.vsu.social_network.user_profile_service.exceptions.server;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
