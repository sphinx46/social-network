package ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.service.cache.ContentCacheService;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentCacheServiceImpl implements ContentCacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String POST_DETAILS_CACHE_NAME = "postDetails";
    private static final String USER_PAGE_DETAILS_CACHE_NAME = "userPageDetails";
    private static final String ALL_PAGE_DETAILS_CACHE_NAME = "allPageDetails";
    private static final int PAGES_TO_INVALIDATE = 5;

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictPostDetails(UUID postId) {
        if (postId == null) {
            log.warn("КЕШ_ПОСТ_ДЕТАЛИ_ИНВАЛИДАЦИЯ_ОШИБКА: " +
                    "postId не может быть null");
            return;
        }

        log.info("КЕШ_ПОСТ_ДЕТАЛИ_ИНВАЛИДАЦИЯ_НАЧАЛО:" +
                " инвалидация деталей поста {}", postId);

        try {
            String postIdStr = postId.toString();
            String pattern = POST_DETAILS_CACHE_NAME + "::post:" + postIdStr + ":*";

            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("КЕШ_ПОСТ_ДЕТАЛИ_ИНВАЛИДАЦИЯ_УСПЕХ: " +
                                "удалено {} ключей для поста {}",
                        keys.size(), postId);
            } else {
                log.debug("КЕШ_ПОСТ_ДЕТАЛИ_КЛЮЧИ_НЕ_НАЙДЕНЫ: " +
                        "ключи для поста {} не найдены", postId);
            }

        } catch (Exception e) {
            log.error("КЕШ_ПОСТ_ДЕТАЛИ_ИНВАЛИДАЦИЯ_ОШИБКА: " +
                            "ошибка при инвалидации деталей поста {}",
                    postId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictPostPages(UUID postId) {
        if (postId == null) {
            log.warn("КЕШ_СТРАНИЦЫ_ПОСТ_ОШИБКА: " +
                    "postId не может быть null");
            return;
        }

        log.info("КЕШ_СТРАНИЦЫ_ПОСТ_ИНВАЛИДАЦИЯ_НАЧАЛО: " +
                "инвалидация страниц с постом {}", postId);

        try {
            deleteFirstPages(USER_PAGE_DETAILS_CACHE_NAME, PAGES_TO_INVALIDATE);
            deleteFirstPages(ALL_PAGE_DETAILS_CACHE_NAME, PAGES_TO_INVALIDATE);

        } catch (Exception e) {
            log.error("КЕШ_СТРАНИЦЫ_ПОСТ_ИНВАЛИДАЦИЯ_ОШИБКА: " +
                            "ошибка при инвалидации страниц с постом {}",
                    postId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictFirstPages() {
        log.info("КЕШ_ПЕРВЫЕ_СТРАНИЦЫ_ИНВАЛИДАЦИЯ_НАЧАЛО: " +
                "инвалидация первых страниц");

        try {
            deleteByPattern(USER_PAGE_DETAILS_CACHE_NAME + "::*page:0*");
            deleteByPattern(USER_PAGE_DETAILS_CACHE_NAME + "::*page:1*");
            deleteByPattern(ALL_PAGE_DETAILS_CACHE_NAME + "::*page:0*");
            deleteByPattern(ALL_PAGE_DETAILS_CACHE_NAME + "::*page:1*");

        } catch (Exception e) {
            log.error("КЕШ_ПЕРВЫЕ_СТРАНИЦЫ_ИНВАЛИДАЦИЯ_ОШИБКА: " +
                    "ошибка при инвалидации первых страниц", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictAllPostDetailsCache() {
        log.warn("КЕШ_ПОЛНАЯ_ИНВАЛИДАЦИЯ_НАЧАЛО: " +
                "полная инвалидация всего кеша деталей постов");

        try {
            Cache postDetailsCache = cacheManager.getCache(POST_DETAILS_CACHE_NAME);
            Cache userPageDetailsCache = cacheManager.getCache(USER_PAGE_DETAILS_CACHE_NAME);
            Cache allPageDetailsCache = cacheManager.getCache(ALL_PAGE_DETAILS_CACHE_NAME);

            if (postDetailsCache != null) postDetailsCache.clear();
            if (userPageDetailsCache != null) userPageDetailsCache.clear();
            if (allPageDetailsCache != null) allPageDetailsCache.clear();

            log.warn("КЕШ_ПОЛНАЯ_ИНВАЛИДАЦИЯ_УСПЕХ: " +
                    "полная инвалидация завершена");

        } catch (Exception e) {
            log.error("КЕШ_ПОЛНАЯ_ИНВАЛИДАЦИЯ_ОШИБКА: " +
                    "ошибка при полной инвалидации кеша", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictUserPages(UUID userId) {
        if (userId == null) {
            log.warn("КЕШ_СТРАНИЦЫ_ПОЛЬЗОВАТЕЛЯ_ОШИБКА: " +
                    "userId не может быть null");
            return;
        }

        log.info("КЕШ_СТРАНИЦЫ_ПОЛЬЗОВАТЕЛЯ_ИНВАЛИДАЦИЯ_НАЧАЛО: " +
                "инвалидация страниц пользователя {}", userId);

        try {
            String userIdStr = userId.toString();
            String pattern = USER_PAGE_DETAILS_CACHE_NAME + "::*user:" + userIdStr + ":*";

            deleteByPattern(pattern);

        } catch (Exception e) {
            log.error("КЕШ_СТРАНИЦЫ_ПОЛЬЗОВАТЕЛЯ_ИНВАЛИДАЦИЯ_ОШИБКА: " +
                            "ошибка при инвалидации страниц пользователя {}",
                    userId, e);
        }
    }

    /**
     * Удаление ключей по шаблону.
     */
    private void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * Инвалидирует первые N страниц в указанном кэше.
     */
    private void deleteFirstPages(String cacheName, int pagesToInvalidate) {
        int deletedTotal = 0;

        for (int page = 0; page < pagesToInvalidate; page++) {
            String pattern = cacheName + "::*page:" + page + "*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                deletedTotal += keys.size();
            }
        }
    }
}