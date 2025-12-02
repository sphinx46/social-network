package ru.cs.vsu.social_network.messaging_service.exception;

public class InterlocutorNotFoundException extends RuntimeException {
    public InterlocutorNotFoundException(String message) {
        super(message);
    }
}
