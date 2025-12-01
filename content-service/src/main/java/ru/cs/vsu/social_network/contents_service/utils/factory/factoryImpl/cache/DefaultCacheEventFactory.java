package ru.cs.vsu.social_network.contents_service.utils.factory.factoryImpl.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;
import ru.cs.vsu.social_network.contents_service.events.cache.GenericCacheEvent;
import ru.cs.vsu.social_network.contents_service.utils.factory.cache.AbstractCacheEventFactory;

import java.util.Map;
import java.util.UUID;

/**
 * Реализация фабрики событий кеша по умолчанию.
 * Создает конкретные события для различных операций с кешем.
 */
@Slf4j
@Component
public class DefaultCacheEventFactory extends AbstractCacheEventFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericCacheEvent createPostEvent(CacheEventType eventType,
                                             Object source,
                                             Object target,
                                             UUID userId,
                                             UUID postId) {
        log.debug("СОБЫТИЕ_ФАБРИКА_ПОСТ_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание события поста типа {} для postId {}",
                eventType, postId);

        Map<String, Object> data = Map.of(
                "userId", userId,
                "postId", postId
        );

        GenericCacheEvent event = createEvent(source, target, eventType, data);

        log.debug("СОБЫТИЕ_ФАБРИКА_ПОСТ_СОЗДАНИЕ_УСПЕХ: " +
                "событие поста создано для postId {}", postId);

        return event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericCacheEvent createCommentEvent(CacheEventType eventType,
                                                Object source,
                                                Object target,
                                                UUID postId,
                                                UUID commentId,
                                                UUID userId) {
        log.debug("СОБЫТИЕ_ФАБРИКА_КОММЕНТАРИЙ_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание события комментария типа {} для commentId {}",
                eventType, commentId);

        Map<String, Object> data = Map.of(
                "postId", postId,
                "commentId", commentId,
                "userId", userId
        );

        GenericCacheEvent event = createEvent(source, target, eventType, data);

        log.debug("СОБЫТИЕ_ФАБРИКА_КОММЕНТАРИЙ_СОЗДАНИЕ_УСПЕХ: " +
                        "событие комментария создано для commentId {}",
                commentId);

        return event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericCacheEvent createPostLikedEvent(CacheEventType eventType,
                                                  Object source,
                                                  Object target,
                                                  UUID postId,
                                                  UUID likeId,
                                                  UUID userId) {
        log.debug("СОБЫТИЕ_ФАБРИКА_ЛАЙК_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание события лайка типа {} для likeId {}",
                eventType, likeId);

        Map<String, Object> data = Map.of(
                "postId", postId,
                "userId", userId,
                "likeId", likeId
        );

        GenericCacheEvent event = createEvent(source, target, eventType, data);

        log.debug("СОБЫТИЕ_ФАБРИКА_ЛАЙК_СОЗДАНИЕ_УСПЕХ: " +
                "событие лайка создано для likeId {}", likeId);

        return event;
    }
}