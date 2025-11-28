package ru.cs.vsu.social_network.contents_service.service.batch;

import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для пакетных операций с комментариями.
 * Обеспечивает эффективное получение комментариев для множества постов.
 */
public interface BatchCommentService {

    /**
     * Получает количество комментариев для списка постов.
     *
     * @param postIds список идентификаторов постов
     * @return маппинг ID поста -> количество комментариев
     */
    Map<UUID, Long> getCommentsCountsForPosts(List<UUID> postIds);

    /**
     * Получает комментарии для списка постов с ограничением по количеству.
     *
     * @param postIds список идентификаторов постов
     * @param commentsLimit лимит комментариев на пост
     * @return маппинг ID поста -> список комментариев
     */
    Map<UUID, List<CommentResponse>> getCommentsForPosts(List<UUID> postIds, int commentsLimit);

    /**
     * Получает комментарии для поста с ограничением по количеству.
     *
     * @param postId идентификатор поста
     * @param limit максимальное количество комментариев
     * @return список комментариев
     */
    List<CommentResponse> getCommentsForPost(UUID postId, int limit);
}