package ru.cs.vsu.social_network.contents_service.validation;

import java.util.UUID;

/**
 * Валидатор для проверки прав доступа к постам.
 * Обеспечивает проверку владения и прав доступа пользователей к операциям с постами.
 */
public interface PostValidator {

    /**
     * Проверяет, является ли пользователь владельцем поста.
     *
     * @param keycloakUserId идентификатор пользователя для проверки
     * @param postId идентификатор поста для проверки прав доступа
     * @throws org.springframework.security.access.AccessDeniedException если пользователь не является владельцем поста
     */
    void validateOwnership(UUID keycloakUserId, UUID postId);
}