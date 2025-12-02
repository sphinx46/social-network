package ru.cs.vsu.social_network.messaging_service.exception;

public class ConversationSelfException extends RuntimeException {
    public ConversationSelfException(String message) {
        super(message);
    }
}
