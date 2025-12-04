package ru.cs.vsu.social_network.messaging_service.utils.factory.cache;


import ru.cs.vsu.social_network.messaging_service.event.GenericMessagingCacheEvent;
import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;

import java.util.UUID;

/**
 * Фабрика для создания событий кеша мессенджера.
 * Обеспечивает создание типизированных событий для различных операций с переписками.
 */
public interface MessagingCacheEventFactory {

    /**
     * Создает событие для операций с сообщениями.
     *
     * @param eventType тип события
     * @param source источник события
     * @param target целевой объект
     * @param conversationId идентификатор беседы
     * @param messageId идентификатор сообщения
     * @param senderId идентификатор отправителя
     * @param receiverId идентификатор получателя
     * @return событие кеша для сообщения
     */
    GenericMessagingCacheEvent createMessageEvent(CacheEventType eventType,
                                                  Object source,
                                                  Object target,
                                                  UUID conversationId,
                                                  UUID messageId,
                                                  UUID senderId,
                                                  UUID receiverId);

    /**
     * Создает событие для операций с беседами.
     *
     * @param eventType тип события
     * @param source источник события
     * @param target целевой объект
     * @param conversationId идентификатор беседы
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return событие кеша для беседы
     */
    GenericMessagingCacheEvent createConversationEvent(CacheEventType eventType,
                                                       Object source,
                                                       Object target,
                                                       UUID conversationId,
                                                       UUID user1Id,
                                                       UUID user2Id);

    /**
     * Создает событие для операций со статусом сообщений.
     *
     * @param eventType тип события
     * @param source источник события
     * @param target целевой объект
     * @param conversationId идентификатор беседы
     * @param userId идентификатор пользователя
     * @return событие кеша для статуса сообщений
     */
    GenericMessagingCacheEvent createMessageStatusEvent(CacheEventType eventType,
                                                        Object source,
                                                        Object target,
                                                        UUID conversationId,
                                                        UUID userId);
}