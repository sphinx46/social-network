package ru.cs.vsu.social_network.messaging_service.utils.factory.messaging;

import java.util.UUID;

/**
 * Обобщенная фабрика.
 * Определяет базовый контракт для создания сущностей на основе запросов.
 *
 * @param <T> тип создаваемой сущности
 * @param <R> тип запроса для создания сущности
 */
public interface GenericFactory<T, R> {

    /**
     * Создает новую сущность на основе данных пользователя и запроса.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param request данные для создания сущности
     * @return созданная сущность
     */
    T create(UUID keycloakUserId, R request);
}