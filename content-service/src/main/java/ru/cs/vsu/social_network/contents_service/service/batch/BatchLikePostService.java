package ru.cs.vsu.social_network.contents_service.service.batch;

import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для пакетных операций с лайками.
 * Обеспечивает эффективное получение лайков для множества постов.
 */
public interface BatchLikePostService {

    /**
     * Получает количество лайков для списка постов.
     *
     * @param postIds список идентификаторов постов
     * @return маппинг ID поста -> количество лайков
     */
    Map<UUID, Long> getLikesCountsForPosts(List<UUID> postIds);

    /**
     * Получает лайки для списка постов с ограничением по количеству.
     *
     * @param postIds    список идентификаторов постов
     * @param likesLimit лимит лайков на пост
     * @return маппинг ID поста -> список лайков
     */
    Map<UUID, List<LikePostResponse>> getLikesForPosts(List<UUID> postIds, int likesLimit);

    /**
     * Получает лайки для поста с ограничением по количеству.
     *
     * @param postId идентификатор поста
     * @param limit  максимальное количество лайков
     * @return список лайков
     */
    List<LikePostResponse> getLikesForPost(UUID postId, int limit);

    /**
     * Получает информацию о лайках пользователя для списка постов.
     * Определяет, какие посты пользователь уже лайкнул.
     *
     * @param ownerId идентификатор пользователя
     * @param postIds список идентификаторов постов
     * @return карта соответствия идентификатора поста к информации о лайке пользователя
     */
    Map<UUID, Boolean> getLikesStatusForPosts(UUID ownerId, List<UUID> postIds);
}