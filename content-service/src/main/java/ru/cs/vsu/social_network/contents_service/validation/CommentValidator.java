package ru.cs.vsu.social_network.contents_service.validation;

import ru.cs.vsu.social_network.contents_service.entity.Comment;

/**
 * Валидатор для проверки прав доступа к комментариям.
 * Обеспечивает проверку владения и прав доступа пользователей к операциям с комментариями.
 */
public interface CommentValidator extends ContentValidator<Comment> {
}