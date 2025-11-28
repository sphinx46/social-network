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
}