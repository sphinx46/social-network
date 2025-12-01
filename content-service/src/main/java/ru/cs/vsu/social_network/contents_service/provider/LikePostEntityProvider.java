package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.LikePost;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Специализированный провайдер для доступа к сущностям LikePost.
 * Обеспечивает получение лайков по идентификатору с обработкой ошибок.
 */
public interface LikePostEntityProvider extends EntityProvider<LikePost> {

    /**
     * Получает количество лайков для поста.
     *
     * @param postId идентификатор поста
     * @return количество лайков
     */
    Long getLikesCountByPost(UUID postId);

    /**
     * Находит лайк поста по идентификатору пользователя и поста.
     *
     * @param ownerId идентификатор пользователя
     * @param postId  идентификатор поста
     * @return Optional с лайком, если найден
     */
    Optional<LikePost> findByOwnerIdAndPostId(UUID ownerId, UUID postId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и поста.
     *
     * @param ownerId идентификатор пользователя
     * @param postId  идентификатор поста
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndPostId(UUID ownerId, UUID postId);

    /**
     * Получает количество лайков для списка постов в пакетном режиме.
     *
     * @param postIds список идентификаторов постов
     * @return маппинг postId -> количество лайков
     */
    Map<UUID, Long> getLikesCountsForPosts(List<UUID> postIds);

    /**
     * Получает лайки с предзагруженными постами для списка идентификаторов.
     *
     * @param postIds список идентификаторов постов
     * @param limit   максимальное количество лайков на пост
     * @return список лайков с загруженными постами
     */
    List<LikePost> getLikesWithPosts(List<UUID> postIds, int limit);

    /**
     * Получает лайки пользователя для списка постов.
     *
     * @param ownerId идентификатор пользователя
     * @param postIds список идентификаторов постов
     * @return список лайков пользователя для указанных постов
     */
    List<LikePost> getLikesByOwnerAndPosts(UUID ownerId, List<UUID> postIds);

    /**
     * Получает последние лайки для поста с ограничением.
     *
     * @param postId идентификатор поста
     * @param limit  максимальное количество возвращаемых лайков
     * @return список последних лайков для указанного поста
     */
    List<LikePost> getRecentLikesForPost(UUID postId, int limit);
}