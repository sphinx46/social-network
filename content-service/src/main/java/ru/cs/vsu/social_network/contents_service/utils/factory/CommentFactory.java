package ru.cs.vsu.social_network.contents_service.utils.factory;

import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentCreateRequest;
import ru.cs.vsu.social_network.contents_service.entity.Comment;

/**
 * Фабрика для создания сущностей Comment.
 * Специализированная реализация для работы с комментариями.
 */
public interface CommentFactory extends ContentFactory<Comment, CommentCreateRequest> {
}