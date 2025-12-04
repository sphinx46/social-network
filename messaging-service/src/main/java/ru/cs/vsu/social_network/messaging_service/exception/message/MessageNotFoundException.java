package ru.cs.vsu.social_network.messaging_service.exception.message;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(String message) {
        super(message);
    }
}
