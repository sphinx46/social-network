package ru.cs.vsu.social_network.messaging_service.exception.conversation;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(String message) {
        super(message);
    }
}
