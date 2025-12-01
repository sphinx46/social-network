package ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;
import ru.cs.vsu.social_network.contents_service.service.cache.CacheEventFallbackService;
import ru.cs.vsu.social_network.contents_service.service.cache.ContentCacheService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Реализация сервиса для обработки отложенной инвалидации кэша при ошибках.
 * Использует периодическую задачу для обработки накопленных инвалидаций.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEventFallbackServiceImpl implements CacheEventFallbackService {

    private final ContentCacheService contentCacheService;
    private final Map<UUID, CacheEventType> pendingInvalidations = new ConcurrentHashMap<>();
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPendingInvalidation(UUID postId, CacheEventType eventType) {
        if (postId == null) {
            log.warn("FALLBACK_РЕГИСТРАЦИЯ_ОШИБКА:" +
                    " postId не может быть null");
            return;
        }

        pendingInvalidations.put(postId, eventType);
        log.warn("FALLBACK_РЕГИСТРАЦИЯ: " +
                        "отложенная инвалидация зарегистрирована для поста {}, тип события {}",
                postId, eventType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeImmediateInvalidation(UUID postId) {
        if (postId == null) {
            log.warn("FALLBACK_НЕМЕДЛЕННАЯ_ОШИБКА: " +
                    "postId не может быть null");
            return;
        }

        log.info("FALLBACK_НЕМЕДЛЕННАЯ_ИНВАЛИДАЦИЯ: " +
                "выполнение немедленной инвалидации для поста {}", postId);
        executeInvalidationWithRetry(postId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPendingInvalidationsCount() {
        return pendingInvalidations.size();
    }

    /**
     * Периодически обрабатывает накопленные инвалидации.
     * Выполняется каждые 30 секунд для очистки очереди отложенных инвалидаций.
     */
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void processPendingInvalidations() {
        if (pendingInvalidations.isEmpty()) {
            return;
        }

        log.info("FALLBACK_ОБРАБОТКА_ОТЛОЖЕННЫХ: " +
                "обработка {} отложенных инвалидаций", pendingInvalidations.size());

        pendingInvalidations.keySet().forEach(postId -> {
            try {
                executeInvalidationWithRetry(postId);
                pendingInvalidations.remove(postId);
                log.info("FALLBACK_ОБРАБОТКА_УСПЕХ: кэш для поста {} успешно инвалидирован", postId);
            } catch (Exception e) {
                log.error("FALLBACK_ОБРАБОТКА_ОШИБКА: ошибка при инвалидации кэша для поста {}", postId, e);
            }
        });
    }

    /**
     * Выполняет инвалидацию кэша с повторными попытками.
     *
     * @param postId ID поста для инвалидации
     */
    private void executeInvalidationWithRetry(UUID postId) {
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRY_ATTEMPTS && !success) {
            attempt++;
            try {
                log.debug("FALLBACK_ПОПЫТКА_{}: " +
                        "инвалидация кэша для поста {}", attempt, postId);

                contentCacheService.evictPostDetails(postId);
                contentCacheService.evictPostPages(postId);

                success = true;
                log.debug("FALLBACK_ПОПЫТКА_УСПЕХ:" +
                        " кэш для поста {} успешно инвалидирован с попытки {}", postId, attempt);

            } catch (Exception e) {
                log.warn("FALLBACK_ПОПЫТКА_ОШИБКА_{}: " +
                                "ошибка при инвалидации кэша для поста {}: {}",
                        attempt, postId, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(100 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!success) {
            log.error("FALLBACK_ПОПЫТКИ_ИСЧЕРПАНЫ: " +
                            "не удалось инвалидировать кэш для поста {} после {} попыток",
                    postId, MAX_RETRY_ATTEMPTS);
        }
    }
}