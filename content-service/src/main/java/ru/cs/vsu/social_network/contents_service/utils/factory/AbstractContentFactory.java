package ru.cs.vsu.social_network.contents_service.utils.factory;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Абстрактная реализация фабрики контента.
 * Предоставляет общую логику для создания сущностей с логированием.
 *
 * @param <T> тип создаваемой сущности
 * @param <R> тип запроса для создания сущности
 */
@Slf4j
public abstract class AbstractContentFactory<T, R> implements ContentFactory<T, R> {

    /**
     * {@inheritDoc}
     */
    @Override
    public T create(UUID keycloakUserId, R request) {
        log.info("{}_ФАБРИКА_СОЗДАНИЕ_НАЧАЛО: создание сущности для пользователя: {}",
                getFactoryName(), keycloakUserId);

        T entity = buildEntity(keycloakUserId, request);

        log.info("{}_ФАБРИКА_СОЗДАНИЕ_УСПЕХ: сущность создана для пользователя: {}",
                getFactoryName(), keycloakUserId);
        return entity;
    }

    /**
     * Создает сущность на основе данных пользователя и запроса.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param request данные для создания сущности
     * @return созданная сущность
     */
    protected abstract T buildEntity(UUID keycloakUserId, R request);

    /**
     * Возвращает имя фабрики для логирования.
     *
     * @return имя фабрики
     */
    protected abstract String getFactoryName();
}