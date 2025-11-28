package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.Comment;

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
}