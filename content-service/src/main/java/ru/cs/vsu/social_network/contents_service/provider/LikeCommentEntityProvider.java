package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.LikeComment;

import java.util.List;
import java.util.Map;
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
     * @param ownerId   идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return Optional с лайком, если найден
     */
    Optional<LikeComment> findByOwnerIdAndCommentId(UUID ownerId, UUID commentId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и комментария.
     *
     * @param ownerId   идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndCommentId(UUID ownerId, UUID commentId);

    /**
     * Получает количество лайков для списка комментариев в пакетном режиме.
     *
     * @param commentIds список идентификаторов комментариев
     * @return маппинг commentId -> количество лайков
     */
    Map<UUID, Long> getLikesCountsForComments(List<UUID> commentIds);

    /**
     * Получает последние лайки для списка комментариев с ограничением на каждый комментарий.
     *
     * @param commentIds список идентификаторов комментариев
     * @param limit      лимит лайков на каждый комментарий
     * @return список лайков
     */
    List<LikeComment> getRecentLikesForComments(List<UUID> commentIds, int limit);

    /**
     * Получает лайки с предзагруженными комментариями для списка идентификаторов.
     *
     * @param commentIds список идентификаторов комментариев
     * @param limit      максимальное количество лайков
     * @return список лайков с загруженными комментариями
     */
    List<LikeComment> getLikesWithComments(List<UUID> commentIds, int limit);

    /**
     * Получает последние лайки для комментария с ограничением.
     *
     * @param commentId идентификатор комментария
     * @param limit     максимальное количество возвращаемых лайков
     * @return список последних лайков для указанного комментария
     */
    List<LikeComment> getRecentLikesForComment(UUID commentId, int limit);
}