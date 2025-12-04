package ru.cs.vsu.social_network.messaging_service.event.handler;

/**
 * Базовый интерфейс для обработчиков событий.
 * Определяет контракт для обработки различных типов событий системы.
 *
 * @param <T> тип события для обработки
 */
public interface EventHandler<T> {

    /**
     * Обрабатывает входящее событие.
     * Выполняет бизнес-логику, связанную с конкретным типом события.
     *
     * @param event событие для обработки
     */
    void handle(T event);
}