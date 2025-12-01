package ru.cs.vsu.social_network.contents_service.service.cache;

import java.util.UUID;

/**
 * Сервис для управления кешем деталей постов.
 * Обеспечивает операции инвалидации кеша PostDetailsResponse.
 */
public interface ContentCacheService {

    /**
     * Инвалидирует кеш деталей конкретного поста.
     * Использует Spring Cache аннотации для автоматической инвалидации.
     *
     * @param postId идентификатор поста
     */
    void evictPostDetails(UUID postId);

    /**
     * Инвалидирует все страницы, содержащие указанный пост.
     * Использует ручную инвалидацию для точного контроля.
     *
     * @param postId идентификатор поста
     */
    void evictPostPages(UUID postId);

    /**
     * Инвалидирует первые страницы пагинации.
     * Вызывается при создании новых постов для инвалидации кеша первых страниц.
     */
    void evictFirstPages();

    /**
     * Инвалидирует весь кеш деталей постов.
     * Использовать с осторожностью в production.
     * Использует Spring Cache аннотации для полной очистки кешей.
     */
    void evictAllPostDetailsCache();

    /**
     * Инвалидирует все страницы пагинации для конкретного пользователя.
     * Вызывается при изменении контента пользователя для инвалидации его страниц.
     *
     * @param userId идентификатор пользователя
     */
    void evictUserPages(UUID userId);
}