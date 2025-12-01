package ru.cs.vsu.social_network.contents_service.events.handler.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.events.cache.GenericCacheEvent;
import ru.cs.vsu.social_network.contents_service.service.cache.ContentCacheService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Обработчик событий инвалидации кеша деталей постов.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEventHandlerImpl implements CacheEventHandler {

    private final ContentCacheService contentCacheService;

    private final Map<UUID, AtomicLong> likeOperationCounters = new ConcurrentHashMap<>();
    private static final int BATCH_LIKE_INVALIDATION_THRESHOLD = 5;

    @Async("cacheTaskExecutor")
    @EventListener
    @Override
    public void handleCacheEvent(GenericCacheEvent event) {
        log.debug("ОБРАБОТЧИК_КЕША_СОБЫТИЕ_НАЧАЛО: " +
                "обработка события типа {}", event.getCacheEventType());

        try {
            switch (event.getCacheEventType()) {
                case POST_CREATED:
                    handlePostCreated(event);
                    break;
                case POST_UPDATED:
                    handlePostUpdated(event);
                    break;
                case COMMENT_ADDED:
                case COMMENT_UPDATED:
                case COMMENT_DELETED:
                    handleCommentEvent(event);
                    break;
                case LIKE_ADDED:
                case LIKE_DELETED:
                    handleLikeEvent(event);
                    break;
                default:
                    log.warn("ОБРАБОТЧИК_КЕША_НЕИЗВЕСТНЫЙ_ТИП:" +
                                    " неизвестный тип события {}",
                            event.getCacheEventType());
            }

            log.debug("ОБРАБОТЧИК_КЕША_СОБЫТИЕ_УСПЕХ: " +
                            "событие типа {} успешно обработано",
                    event.getCacheEventType());

        } catch (Exception e) {
            log.error("ОБРАБОТЧИК_КЕША_СОБЫТИЕ_ОШИБКА:" +
                            " ошибка при обработке события типа {}",
                    event.getCacheEventType(), e);
        }
    }

    private void handlePostCreated(GenericCacheEvent event) {
        UUID postId = event.getData("postId", UUID.class);

        if (postId == null) {
            log.warn("ОБРАБОТЧИК_КЕША_ПОСТ_СОЗДАНИЕ_ОШИБКА: отсутствует postId в событии");
            return;
        }

        log.info("ОБРАБОТЧИК_КЕША_ПОСТ_СОЗДАНИЕ: обработка создания поста {}", postId);

        contentCacheService.evictFirstPages();
    }

    private void handlePostUpdated(GenericCacheEvent event) {
        UUID postId = event.getData("postId", UUID.class);

        if (postId == null) {
            log.warn("ОБРАБОТЧИК_КЕША_ПОСТ_ОБНОВЛЕНИЕ_ОШИБКА: " +
                    "отсутствует postId в событии");
            return;
        }

        log.info("ОБРАБОТЧИК_КЕША_ПОСТ_ОБНОВЛЕНИЕ: " +
                "обработка обновления поста {}", postId);

        contentCacheService.evictPostDetails(postId);

        contentCacheService.evictPostPages(postId);
    }

    private void handleCommentEvent(GenericCacheEvent event) {
        UUID postId = event.getData("postId", UUID.class);

        if (postId == null) {
            log.warn("ОБРАБОТЧИК_КЕША_КОММЕНТАРИЙ_ОШИБКА: " +
                    "отсутствует postId в событии комментария");
            return;
        }

        log.info("ОБРАБОТЧИК_КЕША_КОММЕНТАРИЙ: " +
                        "обработка {} комментария для поста {}",
                event.getCacheEventType(), postId);

        contentCacheService.evictPostDetails(postId);

        contentCacheService.evictPostPages(postId);
    }

    private void handleLikeEvent(GenericCacheEvent event) {
        UUID postId = event.getData("postId", UUID.class);

        if (postId == null) {
            log.warn("ОБРАБОТЧИК_КЕША_ЛАЙК_ОШИБКА: " +
                    "отсутствует postId в событии лайка");
            return;
        }

        log.debug("ОБРАБОТЧИК_КЕША_ЛАЙК: " +
                        "обработка {} лайка для поста {}",
                event.getCacheEventType(), postId);

        contentCacheService.evictPostDetails(postId);

        handleBatchLikeInvalidation(postId);
    }

    private void handleBatchLikeInvalidation(UUID postId) {
        long operationCount = likeOperationCounters
                .computeIfAbsent(postId, k -> new AtomicLong(0))
                .incrementAndGet();

        if (operationCount >= BATCH_LIKE_INVALIDATION_THRESHOLD) {
            log.debug("ОБРАБОТЧИК_КЕША_ЛАЙК_BATCH: " +
                            "выполнение batch инвалидации для поста {} после {} операций",
                    postId, operationCount);

            contentCacheService.evictPostPages(postId);
            likeOperationCounters.get(postId).set(0);
        }
    }
}