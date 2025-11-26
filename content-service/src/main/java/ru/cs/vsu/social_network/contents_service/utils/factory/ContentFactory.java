package ru.cs.vsu.social_network.contents_service.utils.factory;

import java.util.UUID;

/**
 * Обобщенная фабрика для создания контента.
 * Определяет базовый контракт для создания сущностей на основе запросов.
 *
 * @param <T> тип создаваемой сущности
 * @param <R> тип запроса для создания сущности
 */
public interface ContentFactory<T, R> {

    /**
     * Создает новую сущность на основе данных пользователя и запроса.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param request данные для создания сущности
     * @return созданная сущность
     */
    T create(UUID keycloakUserId, R request);
}