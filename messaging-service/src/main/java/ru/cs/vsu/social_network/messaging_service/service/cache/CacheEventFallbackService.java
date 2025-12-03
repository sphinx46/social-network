package ru.cs.vsu.social_network.messaging_service.service.cache;

import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;

import java.util.UUID;

/**
 * Сервис для обработки отложенной инвалидации кэша мессенджера при ошибках.
 * Обеспечивает гарантированную инвалидацию кэша даже в случае сбоев
 * при публикации событий или обработке транзакций.
 */
public interface CacheEventFallbackService {

    /**
     * Регистрирует необходимость инвалидации кэша для беседы.
     * Используется при ошибках публикации событий или обработки транзакций.
     *
     * @param conversationId ID беседы, для которой требуется инвалидация кэша
     * @param eventType тип события, вызвавшего необходимость инвалидации
     */
    void registerPendingInvalidation(UUID conversationId, CacheEventType eventType);

    /**
     * Регистрирует необходимость инвалидации кэша для сообщения.
     * Используется при ошибках публикации событий или обработки транзакций.
     *
     * @param messageId ID сообщения, для которого требуется инвалидация кэша
     * @param eventType тип события, вызвавшего необходимость инвалидации
     */
    void registerPendingInvalidationForMessage(UUID messageId, CacheEventType eventType);

    /**
     * Регистрирует необходимость инвалидации кэша для пользователя.
     * Используется при ошибках публикации событий или обработки транзакций.
     *
     * @param userId ID пользователя, для которого требуется инвалидация кэша
     * @param eventType тип события, вызвавшего необходимость инвалидации
     */
    void registerPendingInvalidationForUser(UUID userId, CacheEventType eventType);

    /**
     * Выполняет немедленную инвалидацию кэша для беседы.
     * Используется в критичных случаях, когда требуется срочная инвалидация.
     *
     * @param conversationId ID беседы, для которой требуется инвалидация кэша
     */
    void executeImmediateInvalidation(UUID conversationId);

    /**
     * Возвращает количество ожидающих инвалидаций.
     *
     * @return количество записей в очереди отложенной инвалидации
     */
    int getPendingInvalidationsCount();

    /**
     * Очищает все ожидающие инвалидации.
     * Используется при перезапуске сервиса или вручную при необходимости.
     */
    void clearAllPendingInvalidations();
}