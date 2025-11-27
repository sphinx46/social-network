package ru.cs.vsu.social_network.contents_service.validation;

import ru.cs.vsu.social_network.contents_service.entity.LikePost;

/**
 * Валидатор для проверки прав доступа к лайкам постов.
 * Обеспечивает проверку владения и прав доступа пользователей к операциям с лайками постов.
 */
public interface LikePostValidator extends ContentValidator<LikePost> {
}