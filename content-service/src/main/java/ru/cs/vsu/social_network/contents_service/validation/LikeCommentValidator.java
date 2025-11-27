package ru.cs.vsu.social_network.contents_service.validation;

import ru.cs.vsu.social_network.contents_service.entity.LikeComment;

/**
 * Валидатор для проверки прав доступа к лайкам комментариев.
 * Обеспечивает проверку владения и прав доступа пользователей к операциям с лайками комментариев.
 */
public interface LikeCommentValidator extends ContentValidator<LikeComment> {
}