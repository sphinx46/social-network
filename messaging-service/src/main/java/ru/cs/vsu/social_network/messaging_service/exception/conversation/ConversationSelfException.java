package ru.cs.vsu.social_network.messaging_service.exception.conversation;

public class ConversationSelfException extends RuntimeException {
    public ConversationSelfException(String message) {
        super(message);
    }
}
