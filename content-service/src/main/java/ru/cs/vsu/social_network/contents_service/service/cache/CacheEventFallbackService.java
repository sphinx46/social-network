package ru.cs.vsu.social_network.contents_service.service.cache;

import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;

import java.util.UUID;

/**
 * Сервис для обработки отложенной инвалидации кэша при ошибках.
 * Обеспечивает гарантированную инвалидацию кэша даже в случае сбоев
 * при публикации событий или обработке транзакций.
 */
public interface CacheEventFallbackService {

    /**
     * Регистрирует необходимость инвалидации кэша для поста.
     * Используется при ошибках публикации событий или обработки транзакций.
     *
     * @param postId ID поста, для которого требуется инвалидация кэша
     * @param eventType тип события, вызвавшего необходимость инвалидации
     */
    void registerPendingInvalidation(UUID postId, CacheEventType eventType);

    /**
     * Выполняет немедленную инвалидацию кэша для поста.
     * Используется в критичных случаях, когда требуется срочная инвалидация.
     *
     * @param postId ID поста, для которого требуется инвалидация кэша
     */
    void executeImmediateInvalidation(UUID postId);

    /**
     * Возвращает количество ожидающих инвалидаций.
     *
     * @return количество записей в очереди отложенной инвалидации
     */
    int getPendingInvalidationsCount();
}