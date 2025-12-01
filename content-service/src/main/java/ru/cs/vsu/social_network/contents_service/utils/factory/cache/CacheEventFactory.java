package ru.cs.vsu.social_network.contents_service.utils.factory.cache;

import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;
import ru.cs.vsu.social_network.contents_service.events.cache.GenericCacheEvent;

import java.util.UUID;

/**
 * Фабрика для создания событий кеша.
 * Обеспечивает создание типизированных событий для различных операций.
 */
public interface CacheEventFactory {

    /**
     * Создает событие для операций с постами.
     *
     * @param eventType тип события
     * @param source источник события
     * @param target целевой объект
     * @param userId идентификатор пользователя
     * @param postId идентификатор поста
     * @return событие кеша для поста
     */
    GenericCacheEvent createPostEvent(CacheEventType eventType,
                                      Object source,
                                      Object target,
                                      UUID userId,
                                      UUID postId);

    /**
     * Создает событие для операций с комментариями.
     *
     * @param eventType тип события
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     * @param userId идентификатор пользователя
     * @return событие кеша для комментария
     */
    GenericCacheEvent createCommentEvent(CacheEventType eventType,
                                         Object source,
                                         Object target,
                                         UUID postId,
                                         UUID commentId,
                                         UUID userId);

    /**
     * Создает событие для операций с лайками постов.
     *
     * @param eventType тип события
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     * @param likeId идентификатор лайка
     * @param userId идентификатор пользователя
     * @return событие кеша для лайка поста
     */
    GenericCacheEvent createPostLikedEvent(CacheEventType eventType,
                                           Object source,
                                           Object target,
                                           UUID postId,
                                           UUID likeId,
                                           UUID userId);
}