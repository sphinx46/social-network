package ru.cs.vsu.social_network.contents_service.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import ru.cs.vsu.social_network.contents_service.provider.EntityProvider;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.UUID;

/**
 * Абстрактная реализация валидатора для проверки прав доступа к контенту.
 * Предоставляет общую логику для проверки владения сущностями с логированием.
 *
 * @param <T> тип сущности для валидации
 */
@Slf4j
public abstract class AbstractContentValidator<T> implements ContentValidator<T> {

    protected final EntityProvider<T> entityProvider;
    protected final String entityName;

    protected AbstractContentValidator(EntityProvider<T> entityProvider,
                                       String entityName) {
        this.entityProvider = entityProvider;
        this.entityName = entityName;
    }

    /** {@inheritDoc} */
    @Override
    public void validateOwnership(UUID keycloakUserId, UUID entityId) {
        log.debug("{}_ВАЛИДАТОР_ПРОВЕРКА_ВЛАДЕНИЯ_НАЧАЛО: " +
                "проверка прав доступа пользователя {} к сущности {}",
                entityName, keycloakUserId, entityId);

        T entity = entityProvider.getById(entityId);
        UUID ownerId = extractOwnerId(entity);

        if (!ownerId.equals(keycloakUserId)) {
            log.warn("{}_ВАЛИДАТОР_ОШИБКА_ДОСТУПА: " +
                            "пользователь {} пытается получить доступ к " +
                            "чужой сущности {} (владелец: {})",
                    entityName, keycloakUserId, entityId, ownerId);

            throw new AccessDeniedException(MessageConstants.ACCESS_DENIED);
        }

        log.debug("{}_ВАЛИДАТОР_ПРОВЕРКА_ВЛАДЕНИЯ_УСПЕХ: " +
                "пользователь {} имеет права доступа к сущности {}",
                entityName, keycloakUserId, entityId);
    }

    /**
     * Извлекает идентификатор владельца из сущности.
     *
     * @param entity сущность для извлечения владельца
     * @return идентификатор владельца сущности
     */
    protected abstract UUID extractOwnerId(T entity);
}