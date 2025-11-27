package ru.cs.vsu.social_network.contents_service.provider;

import java.util.UUID;

/**
 * Абстрактный провайдер для доступа к сущностям.
 * Обеспечивает получение постов по идентификатору с обработкой ошибок.
 */
public interface EntityProvider<T> {
    /**
     * Получает сущность по идентификатору.
     *
     * @param id идентификатор сущности
     * @return найденный пост
     * @throws NotFoundException если сущность не найдена
     */
    T getById(UUID id);
}
