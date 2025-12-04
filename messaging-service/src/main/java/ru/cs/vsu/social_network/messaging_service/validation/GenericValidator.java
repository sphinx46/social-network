package ru.cs.vsu.social_network.messaging_service.validation;

import java.util.UUID;

/**
 * Базовый валидатор для проверки прав доступа к контенту.
 * Обеспечивает общий контракт для проверки владения сущностями.
 *
 * @param <T> тип сущности для валидации
 */
public interface GenericValidator<T> {

    /**
     * Проверяет, является ли пользователь владельцем сущности.
     *
     * @param keycloakUserId идентификатор пользователя для проверки
     * @param entityId идентификатор сущности для проверки прав доступа
     * @throws org.springframework.security.access.AccessDeniedException,
     * если пользователь не является владельцем сущности
     */
    void validateOwnership(UUID keycloakUserId, UUID entityId);
}