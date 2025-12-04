package ru.cs.vsu.social_network.messaging_service.service.cache;

import java.util.UUID;

/**
 * Сервис для управления кешем переписок и сообщений.
 * Предоставляет методы для инвалидации различных типов кешированных данных.
 */
public interface MessagingCacheService {

    /**
     * Инвалидирует кеш деталей конкретной беседы.
     *
     * @param conversationId идентификатор беседы
     */
    void evictConversationDetails(UUID conversationId);

    /**
     * Инвалидирует кеш сообщений в беседе.
     *
     * @param conversationId идентификатор беседы
     */
    void evictConversationMessages(UUID conversationId);

    /**
     * Инвалидирует кеш списка бесед пользователя.
     *
     * @param userId идентификатор пользователя
     */
    void evictUserConversations(UUID userId);

    /**
     * Инвалидирует кеш конкретного сообщения.
     *
     * @param messageId идентификатор сообщения
     */
    void evictMessage(UUID messageId);

    /**
     * Инвалидирует весь кеш переписки между двумя пользователями.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     */
    void evictConversationBetweenUsers(UUID user1Id, UUID user2Id);

    /**
     * Инвалидирует первые страницы пагинации бесед.
     * Используется для обновления кеша при изменении порядка бесед.
     */
    void evictFirstPages();

    /**
     * Полная инвалидация всего кеша мессенджера.
     * Используется в исключительных случаях для полной очистки кеша.
     */
    void evictAllMessagingCache();
}