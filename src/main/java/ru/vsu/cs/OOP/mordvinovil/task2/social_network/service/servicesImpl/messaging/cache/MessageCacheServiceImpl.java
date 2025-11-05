package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.messaging.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging.MessageCacheService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCacheServiceImpl implements MessageCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CentralLogger centralLogger;

    private static final String CACHE_KEY_PREFIX = "conversation::";

    /**
     * Очищает кеш переписки между двумя пользователями
     *
     * @param userId1 идентификатор первого пользователя
     * @param userId2 идентификатор второго пользователя
     */
    @Override
    public void evictConversationCache(Long userId1, Long userId2) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId1", userId1);
        context.put("userId2", userId2);

        centralLogger.logInfo("КЕШ_ПЕРЕПИСКИ_ОЧИСТКА_ДВОИХ",
                "Очистка кеша переписки между двумя пользователями", context);

        try {
            String pattern1 = CACHE_KEY_PREFIX + "conv:" + userId1 + ":" + userId2 + "*";
            String pattern2 = CACHE_KEY_PREFIX + "conv:" + userId2 + ":" + userId1 + "*";

            Set<String> keys1 = redisTemplate.keys(pattern1);
            Set<String> keys2 = redisTemplate.keys(pattern2);

            int totalDeleted = 0;
            if (keys1 != null && !keys1.isEmpty()) {
                redisTemplate.delete(keys1);
                totalDeleted += keys1.size();
            }
            if (keys2 != null && !keys2.isEmpty()) {
                redisTemplate.delete(keys2);
                totalDeleted += keys2.size();
            }

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("deletedKeysCount", totalDeleted);

            centralLogger.logInfo("КЕШ_ПЕРЕПИСКИ_ОЧИЩЕН_ДВОИХ",
                    "Кеш переписки очищен для двух пользователей", successContext);

        } catch (Exception e) {
            centralLogger.logError("КЕШ_ПЕРЕПИСКИ_ОШИБКА_ОЧИСТКИ_ДВОИХ",
                    "Ошибка при очистке кеша переписки для двух пользователей", context, e);
        }
    }

    /**
     * Очищает кеш переписки для конкретного пользователя
     *
     * @param userId идентификатор пользователя
     */
    @Override
    public void evictConversationCacheForUser(Long userId) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);

        centralLogger.logInfo("КЕШ_ПЕРЕПИСКИ_ОЧИСТКА_ПОЛЬЗОВАТЕЛЯ",
                "Очистка кеша переписки для пользователя", context);

        try {
            String pattern = CACHE_KEY_PREFIX + "conv:" + userId + "*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);

                Map<String, Object> successContext = new HashMap<>(context);
                successContext.put("deletedKeysCount", keys.size());

                centralLogger.logInfo("КЕШ_ПЕРЕПИСКИ_ОЧИЩЕН_ПОЛЬЗОВАТЕЛЯ",
                        "Кеш переписки очищен для пользователя", successContext);
            } else {
                centralLogger.logInfo("КЕШ_ПЕРЕПИСКИ_НЕ_НАЙДЕН_ПОЛЬЗОВАТЕЛЯ",
                        "Кеш переписки не найден для пользователя", context);
            }
        } catch (Exception e) {
            centralLogger.logError("КЕШ_ПЕРЕПИСКИ_ОШИБКА_ОЧИСТКИ_ПОЛЬЗОВАТЕЛЯ",
                    "Ошибка при очистке кеша переписки для пользователя", context, e);
        }
    }
}