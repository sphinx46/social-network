package ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;
import ru.cs.vsu.social_network.contents_service.events.cache.GenericCacheEvent;
import ru.cs.vsu.social_network.contents_service.service.cache.CacheEventPublisherService;
import ru.cs.vsu.social_network.contents_service.utils.factory.cache.CacheEventFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Реализация сервиса публикации событий кеша.
 * Обеспечивает асинхронную публикацию событий для инвалидации кеша
 * с гарантией выполнения после коммита транзакции и интеграцией с FallbackService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEventPublisherServiceImpl implements CacheEventPublisherService {
    private final ApplicationEventPublisher eventPublisher;
    private final CacheEventFactory cacheEventFactory;
    private final CacheEventFallbackServiceImpl cacheEventFallbackService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishPostCreated(Object source,
                                   Object target,
                                   UUID userId,
                                   UUID postId) {
        log.info("СОБЫТИЕ_ПУБЛИКАТОР_ПОСТ_СОЗДАНИЕ_ПУБЛИКАЦИЯ: " +
                "публикация события создания поста {}", postId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createPostEvent(CacheEventType.POST_CREATED,
                        source, target, userId, postId), postId));

        log.debug("СОБЫТИЕ_ПУБЛИКАТОР_ПОСТ_СОЗДАНИЕ_ИНИЦИИРОВАНО: " +
                "событие создания поста {} инициировано", postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishPostUpdated(Object source,
                                   Object target,
                                   UUID userId,
                                   UUID postId) {
        log.info("СОБЫТИЕ_ПУБЛИКАТОР_ПОСТ_ОБНОВЛЕНИЕ_ПУБЛИКАЦИЯ: " +
                "публикация события обновления поста {}", postId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createPostEvent(CacheEventType.POST_UPDATED,
                        source, target, userId, postId), postId));

        log.debug("СОБЫТИЕ_ПУБЛИКАТОР_ПОСТ_ОБНОВЛЕНИЕ_ИНИЦИИРОВАНО: " +
                "событие обновления поста {} инициировано", postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishCommentCreated(Object source,
                                      Object target,
                                      UUID postId,
                                      UUID commentId,
                                      UUID userId) {
        log.info("СОБЫТИЕ_ПУБЛИКАТОР_КОММЕНТАРИЙ_СОЗДАНИЕ_ПУБЛИКАЦИЯ:" +
                        " публикация события создания комментария {} для поста {}",
                commentId, postId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createCommentEvent(CacheEventType.COMMENT_ADDED,
                        source, target, postId, commentId, userId), postId));

        log.debug("СОБЫТИЕ_ПУБЛИКАТОР_КОММЕНТАРИЙ_СОЗДАНИЕ_ИНИЦИИРОВАНО: " +
                "событие создания комментария {} инициировано", commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishCommentUpdated(Object source,
                                      Object target,
                                      UUID postId,
                                      UUID commentId,
                                      UUID userId) {
        log.info("СОБЫТИЕ_ПУБЛИКАТОР_КОММЕНТАРИЙ_ОБНОВЛЕНИЕ_ПУБЛИКАЦИЯ: " +
                        "публикация события обновления комментария {} для поста {}",
                commentId, postId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createCommentEvent(CacheEventType.COMMENT_UPDATED,
                        source, target, postId, commentId, userId), postId));

        log.debug("СОБЫТИЕ_ПУБЛИКАТОР_КОММЕНТАРИЙ_ОБНОВЛЕНИЕ_ИНИЦИИРОВАНО: " +
                "событие обновления комментария {} инициировано", commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishCommentDeleted(Object source,
                                      Object target,
                                      UUID postId,
                                      UUID commentId,
                                      UUID userId) {
        log.info("СОБЫТИЕ_ПУБЛИКАТОР_КОММЕНТАРИЙ_УДАЛЕНИЕ_ПУБЛИКАЦИЯ: " +
                        "публикация события удаления комментария {} для поста {}",
                commentId, postId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createCommentEvent(CacheEventType.COMMENT_DELETED,
                        source, target, postId, commentId, userId), postId));

        log.debug("СОБЫТИЕ_ПУБЛИКАТОР_КОММЕНТАРИЙ_УДАЛЕНИЕ_ИНИЦИИРОВАНО: " +
                "событие удаления комментария {} инициировано", commentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishPostLikeCreated(Object source,
                                       Object target,
                                       UUID postId,
                                       UUID likeId,
                                       UUID userId) {
        log.info("СОБЫТИЕ_ПУБЛИКАТОР_ЛАЙК_СОЗДАНИЕ_ПУБЛИКАЦИЯ: " +
                        "публикация события создания лайка {} для поста {}",
                likeId, postId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createPostLikedEvent(CacheEventType.LIKE_ADDED,
                        source, target, postId, likeId, userId), postId));

        log.debug("СОБЫТИЕ_ПУБЛИКАТОР_ЛАЙК_СОЗДАНИЕ_ИНИЦИИРОВАНО: " +
                "событие создания лайка {} инициировано", likeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishPostLikeDeleted(Object source,
                                       Object target,
                                       UUID postId,
                                       UUID likeId,
                                       UUID userId) {
        log.info("СОБЫТИЕ_ПУБЛИКАТОР_ЛАЙК_УДАЛЕНИЕ_ПУБЛИКАЦИЯ:" +
                        " публикация события удаления лайка {} для поста {}",
                likeId, postId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createPostLikedEvent(CacheEventType.LIKE_DELETED,
                        source, target, postId, likeId, userId), postId));

        log.debug("СОБЫТИЕ_ПУБЛИКАТОР_ЛАЙК_УДАЛЕНИЕ_ИНИЦИИРОВАНО: " +
                "событие удаления лайка {} инициировано", likeId);
    }

    /**
     * Публикует событие кеша с использованием TransactionSynchronization.
     * Гарантирует выполнение после коммита транзакции.
     *
     * @param action действие для выполнения после коммита
     */
    private void publishWithTransactionSync(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("СОБЫТИЕ_ПУБЛИКАТОР_ТРАНЗАКЦИЯ_НЕ_АКТИВНА: " +
                    "публикация без транзакции");
            executeActionSafely(action);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    private final AtomicBoolean actionExecuted = new AtomicBoolean(false);

                    @Override
                    public void afterCommit() {
                        if (actionExecuted.compareAndSet(false, true)) {
                            log.debug("СОБЫТИЕ_ПУБЛИКАТОР_ПОСЛЕ_КОММИТА: в" +
                                    "ыполнение после коммита транзакции");
                            executeActionSafely(action);
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            log.warn("СОБЫТИЕ_ПУБЛИКАТОР_ТРАНЗАКЦИЯ_ОТКАТ: " +
                                    "транзакция откатана, событие не публикуется");
                        } else if (!actionExecuted.get()) {
                            log.error("СОБЫТИЕ_ПУБЛИКАТОР_ДЕЙСТВИЕ_НЕ_ВЫПОЛНЕНО: " +
                                    "действие не выполнено после завершения транзакции");
                            executeActionSafely(action);
                        }
                    }
                }
        );
    }

    /**
     * Безопасно выполняет действие с обработкой исключений.
     *
     * @param action действие для выполнения
     */
    private void executeActionSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("СОБЫТИЕ_ПУБЛИКАТОР_ОШИБКА_ВЫПОЛНЕНИЯ: " +
                    "ошибка при выполнении действия", e);
        }
    }

    /**
     * Публикует событие кеша асинхронно с обработкой ошибок и интеграцией с FallbackService.
     *
     * @param event событие кеша для публикации
     * @param postId ID поста для отложенной инвалидации в случае ошибки
     */
    @Async("cacheTaskExecutor")
    protected void publishEventSafely(GenericCacheEvent event, UUID postId) {
        try {
            log.debug("СОБЫТИЕ_ПУБЛИКАЦИЯ_НАЧАЛО: " +
                    "публикация события типа {}", event.getCacheEventType());

            eventPublisher.publishEvent(event);

            log.debug("СОБЫТИЕ_ПУБЛИКАЦИЯ_УСПЕХ: " +
                    "событие типа {} успешно опубликовано", event.getCacheEventType());
        } catch (Exception e) {
            log.error("СОБЫТИЕ_ПУБЛИКАЦИЯ_ОШИБКА: " +
                            "ошибка при публикации события типа {}",
                    event.getCacheEventType(), e);

            cacheEventFallbackService.registerPendingInvalidation(postId, event.getCacheEventType());
        }
    }
}