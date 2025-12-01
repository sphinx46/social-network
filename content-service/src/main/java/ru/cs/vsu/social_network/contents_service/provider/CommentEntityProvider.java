package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.Comment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Специализированный провайдер для доступа к сущностям Comment.
 * Обеспечивает получение комментариев по идентификатору с обработкой ошибок.
 */
public interface CommentEntityProvider extends EntityProvider<Comment> {

    /**
     * Получает количество комментариев для поста.
     *
     * @param postId идентификатор поста
     * @return количество комментариев
     */
    Long getCommentsCountByPost(UUID postId);

    /**
     * Получает количество комментариев для списка постов в пакетном режиме.
     *
     * @param postIds список идентификаторов постов
     * @return маппинг postId -> количество комментариев
     */
    Map<UUID, Long> getCommentsCountsForPosts(List<UUID> postIds);

    /**
     * Получает последние комментарии для списка постов с ограничением на каждый пост.
     *
     * @param postIds список идентификаторов постов
     * @param limit   лимит комментариев на каждый пост
     * @return список комментариев
     */
    List<Comment> getRecentCommentsForPosts(List<UUID> postIds, int limit);

    /**
     * Получает комментарии с предзагруженными постами для списка идентификаторов.
     *
     * @param commentIds список идентификаторов комментариев
     * @param limit      максимальное количество комментариев
     * @return список комментариев с загруженными постами
     */
    List<Comment> getCommentsWithPosts(List<UUID> commentIds, int limit);

    /**
     * Получает последние комментарии для поста с ограничением.
     *
     * @param postId идентификатор поста
     * @param limit  максимальное количество возвращаемых комментариев
     * @return список последних комментариев для указанного поста
     */
    List<Comment> getRecentCommentsForPost(UUID postId, int limit);
}