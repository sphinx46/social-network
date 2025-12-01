package ru.cs.vsu.social_network.contents_service.utils.factory.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;
import ru.cs.vsu.social_network.contents_service.events.cache.GenericCacheEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Абстрактная реализация фабрики событий кеша.
 * Содержит общую логику для создания событий.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCacheEventFactory implements CacheEventFactory {

    /**
     * Создает базовое событие кеша с общими данными.
     *
     * @param source источник события
     * @param target целевой объект
     * @param eventType тип события
     * @param baseData базовые данные события
     * @return созданное событие кеша
     */
    protected GenericCacheEvent createEvent(Object source,
                                            Object target,
                                            CacheEventType eventType,
                                            Map<String, Object> baseData) {
        log.debug("СОБЫТИЕ_ФАБРИКА_СОЗДАНИЕ_НАЧАЛО: создание события типа {} для объекта {}",
                eventType, target != null ? target.getClass().getSimpleName() : "null");

        Map<String, Object> finalData = new HashMap<>(baseData);
        GenericCacheEvent event = new GenericCacheEvent(source, eventType, target, finalData);

        log.debug("СОБЫТИЕ_ФАБРИКА_СОЗДАНИЕ_УСПЕХ: событие создано с {} дополнительными данными",
                finalData.size());

        return event;
    }
}