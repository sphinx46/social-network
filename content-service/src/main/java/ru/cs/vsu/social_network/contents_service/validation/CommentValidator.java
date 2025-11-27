package ru.cs.vsu.social_network.contents_service.validation;


import java.util.UUID;

/**
 * Валидатор для проверки прав доступа к комментариям.
 * Обеспечивает проверку владения и прав доступа пользователей к операциям с комментариями.
 */
public interface CommentValidator {

    /**
     * Проверяет, является ли пользователь владельцем комментария.
     *
     * @param keycloakUserId идентификатор пользователя для проверки
     * @param commentId идентификатор комментария для проверки прав доступа
     * @throws org.springframework.security.access.AccessDeniedException,
     * если пользователь не является владельцем комментария
     */
    void validateOwnership(UUID keycloakUserId, UUID commentId);
}