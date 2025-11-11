package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
public class GenericCacheEvent extends ApplicationEvent {
    private final Object targetObject;
    private final Map<String, Object> additionalData;
    private final LocalDateTime timeCreated;

    public GenericCacheEvent(Object source, Object targetObject,
                             Map<String, Object> additionalData) {
        super(source);
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
}
