package ru.cs.vsu.social_network.messaging_service.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractEntityProvider<T> implements EntityProvider<T> {
    protected final JpaRepository<T, UUID> repository;
    protected final String entityName;
    protected final Supplier<RuntimeException> notFoundExceptionSupplier;

    /** {@inheritDoc} */
    @Override
    public T getById(UUID id) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_НАЧАЛО: запрос сущности с ID: {}", entityName, id);

        T entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ОШИБКА: сущность с ID: {} не найдена", entityName, id);
                    return notFoundExceptionSupplier.get();
                });

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_УСПЕХ: сущность с ID: {} найдена", entityName, id);
        return entity;
    }
}