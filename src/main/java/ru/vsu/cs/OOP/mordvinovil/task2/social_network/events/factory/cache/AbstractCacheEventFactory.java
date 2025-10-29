package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory.cache;

import lombok.RequiredArgsConstructor;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.GenericCacheEvent;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractCacheEventFactory implements CacheEventFactory {
    /**
     * Базовый метод для создания события с общими данными
     */
    protected GenericCacheEvent createEvent(Object source, Object target,
                                            Map<String, Object> baseData) {
        Map<String, Object> finalData = new HashMap<>(baseData);
        return new GenericCacheEvent(source, target, finalData);
    }
}
