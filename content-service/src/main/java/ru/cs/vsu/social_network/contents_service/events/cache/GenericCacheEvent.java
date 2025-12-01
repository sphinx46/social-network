package ru.cs.vsu.social_network.contents_service.events.cache;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Базовое событие для инвалидации кеша.
 * Содержит информацию о типе события, целевом объекте и дополнительных данных.
 */
@Getter
public class GenericCacheEvent extends ApplicationEvent {
    private final CacheEventType cacheEventType;
    private final Object targetObject;
    private final Map<String, Object> additionalData;
    private final LocalDateTime timeCreated;

    /**
     * Создает новое событие кеша.
     *
     * @param source источник события
     * @param cacheEventType тип события кеша
     * @param targetObject целевой объект события
     * @param additionalData дополнительные данные события
     */
    public GenericCacheEvent(Object source,
                             CacheEventType cacheEventType,
                             Object targetObject,
                             Map<String, Object> additionalData) {
        super(source);
        this.cacheEventType = cacheEventType;
        this.targetObject = targetObject;
        this.additionalData = additionalData != null ? new HashMap<>(additionalData) : new HashMap<>();
        this.timeCreated = LocalDateTime.now();
    }

    /**
     * Добавляет дополнительные данные к событию.
     *
     * @param key ключ данных
     * @param value значение данных
     */
    public void addData(String key, Object value) {
        this.additionalData.put(key, value);
    }

    /**
     * Получает данные события по ключу.
     *
     * @param key ключ данных
     * @return значение данных или null если ключ не найден
     */
    public Object getData(String key) {
        return this.additionalData.get(key);
    }

    /**
     * Получает типизированные данные события по ключу.
     *
     * @param key ключ данных
     * @param type класс типа данных
     * @param <T> тип данных
     * @return типизированное значение данных или null если ключ не найден
     */
    public <T> T getData(String key, Class<T> type) {
        Object value = this.additionalData.get(key);
        return value != null ? type.cast(value) : null;
    }
}