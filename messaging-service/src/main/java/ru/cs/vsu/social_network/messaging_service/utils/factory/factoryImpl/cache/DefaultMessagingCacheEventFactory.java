package ru.cs.vsu.social_network.messaging_service.utils.factory.factoryImpl.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.event.GenericMessagingCacheEvent;
import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;
import ru.cs.vsu.social_network.messaging_service.utils.factory.cache.AbstractMessagingCacheEventFactory;

import java.util.Map;
import java.util.UUID;

/**
 * Реализация фабрики событий кеша мессенджера по умолчанию.
 * Создает конкретные события для различных операций с переписками.
 */
@Slf4j
@Component
public class DefaultMessagingCacheEventFactory extends AbstractMessagingCacheEventFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericMessagingCacheEvent createMessageEvent(CacheEventType eventType,
                                                         Object source,
                                                         Object target,
                                                         UUID conversationId,
                                                         UUID messageId,
                                                         UUID senderId,
                                                         UUID receiverId) {
        log.debug("СОБЫТИЕ_ФАБРИКА_МЕССЕНДЖЕР_СООБЩЕНИЕ_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание события сообщения типа {} для messageId {}",
                eventType, messageId);

        Map<String, Object> data = Map.of(
                "conversationId", conversationId,
                "messageId", messageId,
                "senderId", senderId,
                "receiverId", receiverId
        );

        GenericMessagingCacheEvent event = createEvent(source, target, eventType, data);

        log.debug("СОБЫТИЕ_ФАБРИКА_МЕССЕНДЖЕР_СООБЩЕНИЕ_СОЗДАНИЕ_УСПЕХ: " +
                "событие сообщения создано для messageId {}", messageId);

        return event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericMessagingCacheEvent createConversationEvent(CacheEventType eventType,
                                                              Object source,
                                                              Object target,
                                                              UUID conversationId,
                                                              UUID user1Id,
                                                              UUID user2Id) {
        log.debug("СОБЫТИЕ_ФАБРИКА_МЕССЕНДЖЕР_БЕСЕДА_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание события беседы типа {} для conversationId {}",
                eventType, conversationId);

        Map<String, Object> data = Map.of(
                "conversationId", conversationId,
                "user1Id", user1Id,
                "user2Id", user2Id
        );

        GenericMessagingCacheEvent event = createEvent(source, target, eventType, data);

        log.debug("СОБЫТИЕ_ФАБРИКА_МЕССЕНДЖЕР_БЕСЕДА_СОЗДАНИЕ_УСПЕХ: " +
                "событие беседы создано для conversationId {}", conversationId);

        return event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericMessagingCacheEvent createMessageStatusEvent(CacheEventType eventType,
                                                               Object source,
                                                               Object target,
                                                               UUID conversationId,
                                                               UUID userId) {
        log.debug("СОБЫТИЕ_ФАБРИКА_МЕССЕНДЖЕР_СТАТУС_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание события статуса типа {} для conversationId {}",
                eventType, conversationId);

        Map<String, Object> data = Map.of(
                "conversationId", conversationId,
                "userId", userId
        );

        GenericMessagingCacheEvent event = createEvent(source, target, eventType, data);

        log.debug("СОБЫТИЕ_ФАБРИКА_МЕССЕНДЖЕР_СТАТУС_СОЗДАНИЕ_УСПЕХ: " +
                "событие статуса создано для conversationId {}", conversationId);

        return event;
    }
}