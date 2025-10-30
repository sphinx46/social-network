package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.feed.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging.MessageCacheService;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCacheServiceImpl implements MessageCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "conversation::";

    @Override
    public void evictConversationCache(Long userId1, Long userId2) {
        try {
            String pattern1 = CACHE_KEY_PREFIX + "conv:" + userId1 + ":" + userId2 + "*";
            String pattern2 = CACHE_KEY_PREFIX + "conv:" + userId2 + ":" + userId1 + "*";

            Set<String> keys1 = redisTemplate.keys(pattern1);
            Set<String> keys2 = redisTemplate.keys(pattern2);

            if (keys1 != null && !keys1.isEmpty()) {
                redisTemplate.delete(keys1);
            }
            if (keys2 != null && !keys2.isEmpty()) {
                redisTemplate.delete(keys2);
            }

            log.debug("Кеш переписки инвалидирован для пользователей {} и {}", userId1, userId2);
        } catch (Exception e) {
            log.error("Ошибка при инвалидации кеша переписки для пользователей {} и {}: {}",
                    userId1, userId2, e.getMessage(), e);
        }
    }

    @Override
    public void evictConversationCacheForUser(Long userId) {
        try {
            String pattern = CACHE_KEY_PREFIX + "conv:" + userId + "*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Удалено {} кешей переписки для пользователя id={}", keys.size(), userId);
            }
        } catch (Exception e) {
            log.error("Ошибка при очистке кеша переписки для пользователя id={}: {}", userId, e.getMessage(), e);
        }
    }
}