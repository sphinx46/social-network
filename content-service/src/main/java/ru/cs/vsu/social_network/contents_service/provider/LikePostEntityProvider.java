package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.LikePost;

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
     * @param postId идентификатор поста
     * @return Optional с лайком, если найден
     */
    Optional<LikePost> findByOwnerIdAndPostId(UUID ownerId, UUID postId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и поста.
     *
     * @param ownerId идентификатор пользователя
     * @param postId идентификатор поста
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndPostId(UUID ownerId, UUID postId);
}