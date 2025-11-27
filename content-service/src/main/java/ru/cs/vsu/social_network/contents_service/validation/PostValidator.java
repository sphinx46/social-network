package ru.cs.vsu.social_network.contents_service.validation;

import ru.cs.vsu.social_network.contents_service.entity.Post;

/**
 * Валидатор для проверки прав доступа к постам.
 * Обеспечивает проверку владения и прав доступа пользователей к операциям с постами.
 */
public interface PostValidator extends ContentValidator<Post> {
}