package ru.cs.vsu.social_network.messaging_service.event;

/**
 * Типы событий для инвалидации кеша мессенджера.
 */
public enum CacheEventType {
    MESSAGE_CREATED,
    MESSAGE_UPDATED,
    MESSAGE_DELETED,
    CONVERSATION_CREATED,
    MESSAGES_READ,
    MESSAGE_IMAGE_UPLOADED;

    public String getValue() {
        return this.name();
    }

    public static boolean isValid(String eventType) {
        if (eventType == null) return false;
        try {
            CacheEventType.valueOf(eventType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}