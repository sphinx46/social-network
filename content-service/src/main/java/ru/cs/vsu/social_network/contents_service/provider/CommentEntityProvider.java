package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.Comment;

/**
 * Специализированный провайдер для доступа к сущностям Comment.
 * Обеспечивает получение постов по идентификатору с обработкой ошибок.
 */
public interface CommentEntityProvider extends EntityProvider<Comment> {
}
