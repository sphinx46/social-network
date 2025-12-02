package ru.cs.vsu.social_network.messaging_service.exception.conversation;

public class InterlocutorNotFoundException extends RuntimeException {
    public InterlocutorNotFoundException(String message) {
        super(message);
    }
}
