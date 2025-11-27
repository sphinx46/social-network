package ru.cs.vsu.social_network.contents_service.validation.event;

/**
 * Базовый интерфейс для валидации событий.
 * Определяет контракт для проверки корректности событий системы.
 *
 * @param <T> тип события для валидации
 */
public interface EventValidator<T> {

    /**
     * Выполняет валидацию события.
     * Проверяет корректность данных события и выбрасывает исключение при нарушении правил.
     *
     * @param event событие для валидации
     * @throws IllegalArgumentException если событие не проходит валидацию
     */
    void validateEvent(T event);
}