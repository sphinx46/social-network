package ru.cs.vsu.social_network.messaging_service.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Базовое событие для инвалидации кеша мессенджера.
 */
@Getter
@Setter
@ToString
public class GenericMessagingCacheEvent extends ApplicationEvent {
    private final CacheEventType cacheEventType;
    private final Object targetObject;
    private final Map<String, Object> additionalData;
    private final LocalDateTime timeCreated;

    public GenericMessagingCacheEvent(Object source,
                                      CacheEventType cacheEventType,
                                      Object targetObject,
                                      Map<String, Object> additionalData) {
        super(source);
        this.cacheEventType = cacheEventType;
        this.targetObject = targetObject;
        this.additionalData = additionalData != null ? new HashMap<>(additionalData) : new HashMap<>();
        this.timeCreated = LocalDateTime.now();
    }

    public void addData(String key, Object value) {
        this.additionalData.put(key, value);
    }

    public Object getData(String key) {
        return this.additionalData.get(key);
    }

    public <T> T getData(String key, Class<T> type) {
        Object value = this.additionalData.get(key);
        return value != null ? type.cast(value) : null;
    }

    public static GenericMessagingCacheEventBuilder builder() {
        return new GenericMessagingCacheEventBuilder();
    }

    public static class GenericMessagingCacheEventBuilder {
        private Object source;
        private CacheEventType cacheEventType;
        private Object targetObject;
        private Map<String, Object> additionalData;
        private LocalDateTime timeCreated;

        public GenericMessagingCacheEventBuilder source(Object source) {
            this.source = source;
            return this;
        }

        public GenericMessagingCacheEventBuilder cacheEventType(CacheEventType cacheEventType) {
            this.cacheEventType = cacheEventType;
            return this;
        }

        public GenericMessagingCacheEventBuilder targetObject(Object targetObject) {
            this.targetObject = targetObject;
            return this;
        }

        public GenericMessagingCacheEventBuilder additionalData(Map<String, Object> additionalData) {
            this.additionalData = additionalData;
            return this;
        }

        public GenericMessagingCacheEventBuilder timeCreated(LocalDateTime timeCreated) {
            this.timeCreated = timeCreated;
            return this;
        }

        public GenericMessagingCacheEvent build() {
            return new GenericMessagingCacheEvent(
                    this.source != null ? this.source : new Object(),
                    this.cacheEventType,
                    this.targetObject,
                    this.additionalData
            );
        }
    }
}