package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.LikeComment;

import java.util.Optional;
import java.util.UUID;

/**
 * Специализированный провайдер для доступа к сущностям LikeComment.
 * Обеспечивает получение лайков по идентификатору с обработкой ошибок.
 */
public interface LikeCommentEntityProvider extends EntityProvider<LikeComment> {

    /**
     * Получает количество лайков для комментария.
     *
     * @param commentId идентификатор комментария
     * @return количество лайков
     */
    Long getLikesCountByComment(UUID commentId);

    /**
     * Находит лайк комментария по идентификатору пользователя и комментария.
     *
     * @param ownerId идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return Optional с лайком, если найден
     */
    Optional<LikeComment> findByOwnerIdAndCommentId(UUID ownerId, UUID commentId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и комментария.
     *
     * @param ownerId идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndCommentId(UUID ownerId, UUID commentId);
}