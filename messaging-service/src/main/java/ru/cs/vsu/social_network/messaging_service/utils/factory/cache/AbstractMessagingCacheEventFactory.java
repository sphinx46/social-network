package ru.cs.vsu.social_network.messaging_service.utils.factory.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.cs.vsu.social_network.messaging_service.event.GenericMessagingCacheEvent;
import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;

import java.util.HashMap;
import java.util.Map;

/**
 * Абстрактная реализация фабрики событий кеша мессенджера.
 * Содержит общую логику для создания событий.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMessagingCacheEventFactory implements MessagingCacheEventFactory {

    /**
     * Создает базовое событие кеша с общими данными.
     *
     * @param source источник события
     * @param target целевой объект
     * @param eventType тип события
     * @param baseData базовые данные события
     * @return созданное событие кеша
     */
    protected GenericMessagingCacheEvent createEvent(Object source,
                                                     Object target,
                                                     CacheEventType eventType,
                                                     Map<String, Object> baseData) {
        log.debug("СОБЫТИЕ_ФАБРИКА_МЕССЕНДЖЕР_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание события типа {} для объекта {}",
                eventType, target != null ? target.getClass().getSimpleName() : "null");

        Map<String, Object> finalData = new HashMap<>(baseData);
        GenericMessagingCacheEvent event = new GenericMessagingCacheEvent(source, eventType, target, finalData);

        log.debug("СОБЫТИЕ_ФАБРИКА_МЕССЕНДЖЕР_СОЗДАНИЕ_УСПЕХ: " +
                        "событие создано с {} дополнительными данными",
                finalData.size());

        return event;
    }
}