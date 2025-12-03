package ru.cs.vsu.social_network.messaging_service.event.handler.cache;

import ru.cs.vsu.social_network.messaging_service.event.GenericMessagingCacheEvent;

/**
 * Обработчик событий инвалидации кеша мессенджера.
 * Обеспечивает обработку событий, влияющих на кеш сообщений и бесед.
 */
public interface MessagingCacheEventHandler {

    /**
     * Обрабатывает событие инвалидации кеша мессенджера.
     * Выполняет асинхронную инвалидацию кеша сообщений и бесед.
     *
     * @param event событие кеша для обработки
     */
    void handleMessagingCacheEvent(GenericMessagingCacheEvent event);
}