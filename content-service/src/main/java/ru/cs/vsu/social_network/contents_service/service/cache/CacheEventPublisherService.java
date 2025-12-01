package ru.cs.vsu.social_network.contents_service.service.cache;

import java.util.UUID;

/**
 * Сервис публикации событий инвалидации кэша.
 * Обеспечивает асинхронную публикацию событий для инвалидации кэша с гарантией выполнения после коммита транзакции.
 * Использует TransactionSynchronization для правильного порядка операций.
 */
public interface CacheEventPublisherService {

    /**
     * Публикует событие создания поста.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source источник события
     * @param target целевая сущность
     * @param userId ID пользователя
     * @param postId ID поста
     */
    void publishPostCreated(Object source,
                            Object target,
                            UUID userId,
                            UUID postId);

    /**
     * Публикует событие обновления поста.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source источник события
     * @param target целевая сущность
     * @param userId ID пользователя
     * @param postId ID поста
     */
    void publishPostUpdated(Object source,
                            Object target,
                            UUID userId,
                            UUID postId);

    /**
     * Публикует событие создания комментария.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source источник события
     * @param target целевая сущность
     * @param postId ID поста
     * @param commentId ID комментария
     * @param userId ID пользователя
     */
    void publishCommentCreated(Object source,
                               Object target,
                               UUID postId,
                               UUID commentId,
                               UUID userId);

    /**
     * Публикует событие обновления комментария.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source источник события
     * @param target целевая сущность
     * @param postId ID поста
     * @param commentId ID комментария
     * @param userId ID пользователя
     */
    void publishCommentUpdated(Object source,
                               Object target,
                               UUID postId,
                               UUID commentId,
                               UUID userId);

    /**
     * Публикует событие удаления комментария.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source источник события
     * @param target целевая сущность
     * @param postId ID поста
     * @param commentId ID комментария
     * @param userId ID пользователя
     */
    void publishCommentDeleted(Object source,
                               Object target,
                               UUID postId,
                               UUID commentId,
                               UUID userId);

    /**
     * Публикует событие создания лайка поста.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source источник события
     * @param target целевая сущность
     * @param postId ID поста
     * @param likeId ID лайка
     * @param userId ID пользователя
     */
    void publishPostLikeCreated(Object source,
                                Object target,
                                UUID postId,
                                UUID likeId,
                                UUID userId);

    /**
     * Публикует событие удаления лайка поста.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source источник события
     * @param target целевая сущность
     * @param postId ID поста
     * @param likeId ID лайка
     * @param userId ID пользователя
     */
    void publishPostLikeDeleted(Object source,
                                Object target,
                                UUID postId,
                                UUID likeId,
                                UUID userId);
}