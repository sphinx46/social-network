package ru.cs.vsu.social_network.contents_service.service.batch;

import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для пакетных операций с лайками комментариев.
 * Обеспечивает эффективное получение лайков для множества комментариев.
 */
public interface BatchLikeCommentService {

    /**
     * Получает количество лайков для списка комментариев.
     *
     * @param commentIds список идентификаторов комментариев
     * @return маппинг ID комментария -> количество лайков
     */
    Map<UUID, Long> getLikesCountsForComments(List<UUID> commentIds);

    /**
     * Получает лайки для списка комментариев с ограничением по количеству.
     *
     * @param commentIds список идентификаторов комментариев
     * @param likesLimit лимит лайков на комментарий
     * @return маппинг ID комментария -> список лайков
     */
    Map<UUID, List<LikeCommentResponse>> getLikesForComments(List<UUID> commentIds, int likesLimit);

    /**
     * Получает лайки для комментария с ограничением по количеству.
     *
     * @param commentId идентификатор комментария
     * @param limit     максимальное количество лайков
     * @return список лайков
     */
    List<LikeCommentResponse> getLikesForComment(UUID commentId, int limit);

    /**
     * Получает информацию о лайках пользователя для списка комментариев.
     * Определяет, какие комментарии пользователь уже лайкнул.
     *
     * @param ownerId    идентификатор пользователя
     * @param commentIds список идентификаторов комментариев
     * @return карта соответствия идентификатора комментария к информации о лайке пользователя
     */
    Map<UUID, Boolean> getLikesStatusForComments(UUID ownerId, List<UUID> commentIds);

    /**
     * Получает лайки с предзагруженными комментариями для списка идентификаторов.
     * Устраняет проблему N+1 при доступе к связанным комментариям.
     *
     * @param commentIds список идентификаторов комментариев
     * @param limit      максимальное количество лайков
     * @return список лайков с информацией о комментариях
     */
    List<LikeCommentResponse> getLikesWithComments(List<UUID> commentIds, int limit);
}