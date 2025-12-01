package ru.cs.vsu.social_network.contents_service.events.handler.cache;

import ru.cs.vsu.social_network.contents_service.events.cache.GenericCacheEvent;

/**
 * Обработчик событий инвалидации кеша деталей постов.
 * Обеспечивает обработку событий, влияющих на кеш PostDetailsResponse.
 */
public interface CacheEventHandler {

    /**
     * Обрабатывает событие инвалидации кеша.
     * Выполняет асинхронную инвалидацию кеша деталей постов.
     *
     * @param event событие кеша для обработки
     */
    void handleCacheEvent(GenericCacheEvent event);
}