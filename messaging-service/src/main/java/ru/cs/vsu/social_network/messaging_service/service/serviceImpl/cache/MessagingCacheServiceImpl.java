package ru.cs.vsu.social_network.messaging_service.service.serviceImpl.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheService;

import java.util.Set;
import java.util.UUID;

/**
 * Реализация сервиса для управления кэшем переписок и сообщений.
 * Обеспечивает асинхронную инвалидацию кэша с обработкой ошибок и логированием.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingCacheServiceImpl implements MessagingCacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CONVERSATION_DETAILS_CACHE = "conversationDetails";
    private static final String USER_CONVERSATIONS_CACHE = "userConversations";
    private static final String CONVERSATION_MESSAGES_CACHE = "conversationMessages";
    private static final String MESSAGE_CACHE = "message";
    private static final int PAGES_TO_INVALIDATE = 3;

    private static final String CACHE_KEY_SEPARATOR = "::";
    private static final String ANY_PREFIX = "*";
    private static final String USER_PREFIX = "user:";
    private static final String CONVERSATION_PREFIX = "conversation:";
    private static final String MESSAGE_PREFIX = "message:";
    private static final String PAGE_PREFIX = "page:";

    /**
     * Удаляет ключи из Redis по заданным паттернам.
     *
     * @param patterns массив паттернов для поиска ключей
     * @param logPrefix префикс для логирования
     * @return общее количество удаленных ключей
     */
    private int deleteKeysByPatterns(final String[] patterns, final String logPrefix) {
        int totalDeleted = 0;
        for (final String pattern : patterns) {
            final Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                final Long deletedCount = redisTemplate.delete(keys);
                totalDeleted += deletedCount != null ? deletedCount.intValue() : 0;
            }
        }
        return totalDeleted;
    }

    /**
     * Создает массив паттернов для поиска ключей пользователя.
     * Включает все возможные форматы ключей для бесед пользователя.
     *
     * @param userId идентификатор пользователя
     * @param includePagePatterns флаг для включения паттернов с пагинацией
     * @return массив паттернов для поиска
     */
    private String[] createUserPatterns(final UUID userId, final boolean includePagePatterns) {
        final String userIdStr = userId.toString();
        final String userIdPattern = ANY_PREFIX + userIdStr + ANY_PREFIX;
        final String userPrefixPattern = ANY_PREFIX + USER_PREFIX + userIdStr + ANY_PREFIX;

        final String[] basePatterns = {
                USER_CONVERSATIONS_CACHE + CACHE_KEY_SEPARATOR + userPrefixPattern,
                USER_CONVERSATIONS_CACHE + CACHE_KEY_SEPARATOR + userIdPattern,
                "userConversations" + CACHE_KEY_SEPARATOR + userPrefixPattern,
                "userConversations" + CACHE_KEY_SEPARATOR + userIdPattern,
                ANY_PREFIX + "userConversations" + ANY_PREFIX + userIdStr + ANY_PREFIX,
                userIdStr + ANY_PREFIX + "detailed" + ANY_PREFIX,
                ANY_PREFIX + "detailed" + ANY_PREFIX + userIdStr + ANY_PREFIX,
                ANY_PREFIX + "conversation" + ANY_PREFIX + userIdStr + ANY_PREFIX,
                ANY_PREFIX + "conversations" + ANY_PREFIX + userIdStr + ANY_PREFIX
        };

        if (!includePagePatterns) {
            return basePatterns;
        }

        final java.util.List<String> allPatterns = new java.util.ArrayList<>(java.util.Arrays.asList(basePatterns));

        for (int page = 0; page < PAGES_TO_INVALIDATE; page++) {
            final String pageSuffix = ANY_PREFIX + PAGE_PREFIX + page + ANY_PREFIX;
            allPatterns.add(USER_CONVERSATIONS_CACHE + CACHE_KEY_SEPARATOR + userPrefixPattern + pageSuffix);
            allPatterns.add(USER_CONVERSATIONS_CACHE + CACHE_KEY_SEPARATOR + userIdPattern + pageSuffix);
            allPatterns.add(ANY_PREFIX + "userConversations" + ANY_PREFIX + userIdStr + ANY_PREFIX + pageSuffix);
            allPatterns.add(ANY_PREFIX + "conversation" + ANY_PREFIX + userIdStr + ANY_PREFIX + pageSuffix);
            allPatterns.add(userIdStr + ANY_PREFIX + "detailed" + ANY_PREFIX + pageSuffix);
            allPatterns.add(userIdStr + ANY_PREFIX + pageSuffix);
        }

        return allPatterns.toArray(new String[0]);
    }

    /**
     * Создает массив паттернов для поиска ключей беседы.
     * Включает все возможные форматы ключей для беседы.
     *
     * @param conversationId идентификатор беседы
     * @param cacheType тип кэша (details или messages)
     * @return массив паттернов для поиска
     */
    private String[] createConversationPatterns(final UUID conversationId, final String cacheType) {
        final String conversationIdStr = conversationId.toString();
        final String conversationIdPattern = ANY_PREFIX + conversationIdStr + ANY_PREFIX;
        final String conversationPrefixPattern = ANY_PREFIX + CONVERSATION_PREFIX + conversationIdStr + ANY_PREFIX;

        final String[] commonPatterns = {
                CONVERSATION_DETAILS_CACHE + CACHE_KEY_SEPARATOR + conversationPrefixPattern,
                CONVERSATION_DETAILS_CACHE + CACHE_KEY_SEPARATOR + conversationIdPattern,
                CONVERSATION_DETAILS_CACHE + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                "conversationDetails" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                ANY_PREFIX + "conversationDetails" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                CONVERSATION_MESSAGES_CACHE + CACHE_KEY_SEPARATOR + conversationPrefixPattern,
                CONVERSATION_MESSAGES_CACHE + CACHE_KEY_SEPARATOR + conversationIdPattern,
                CONVERSATION_MESSAGES_CACHE + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                "conversationMessages" + CACHE_KEY_SEPARATOR + conversationIdPattern,
                "conversationMessages" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                ANY_PREFIX + "conversationMessages" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                ANY_PREFIX + CACHE_KEY_SEPARATOR + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                ANY_PREFIX + "messages" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                ANY_PREFIX + "user1:" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                ANY_PREFIX + "user2:" + ANY_PREFIX + conversationIdStr + ANY_PREFIX
        };

        if ("messages".equals(cacheType)) {
            final String[] messagePatterns = {
                    CONVERSATION_MESSAGES_CACHE + CACHE_KEY_SEPARATOR + conversationPrefixPattern,
                    CONVERSATION_MESSAGES_CACHE + CACHE_KEY_SEPARATOR + conversationIdPattern,
                    CONVERSATION_MESSAGES_CACHE + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                    "conversationMessages" + CACHE_KEY_SEPARATOR + conversationIdPattern,
                    "conversationMessages" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                    ANY_PREFIX + "conversationMessages" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                    ANY_PREFIX + CACHE_KEY_SEPARATOR + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                    ANY_PREFIX + "messages" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                    ANY_PREFIX + "user1:" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                    ANY_PREFIX + "user2:" + ANY_PREFIX + conversationIdStr + ANY_PREFIX
            };
            return messagePatterns;
        } else {
            final String[] detailsPatterns = {
                    CONVERSATION_DETAILS_CACHE + CACHE_KEY_SEPARATOR + conversationPrefixPattern,
                    CONVERSATION_DETAILS_CACHE + CACHE_KEY_SEPARATOR + conversationIdPattern,
                    CONVERSATION_DETAILS_CACHE + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                    "conversationDetails" + ANY_PREFIX + conversationIdStr + ANY_PREFIX,
                    ANY_PREFIX + "conversationDetails" + ANY_PREFIX + conversationIdStr + ANY_PREFIX
            };
            return detailsPatterns;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictConversationDetails(final UUID conversationId) {
        final String logPrefix = "КЕШ_БЕСЕДА_ДЕТАЛИ_ИНВАЛИДАЦИЯ";

        if (conversationId == null) {
            log.warn("{}: conversationId не может быть null", logPrefix);
            return;
        }

        log.info("{}: инвалидация деталей беседы {}", logPrefix, conversationId);

        try {
            final String[] patterns = createConversationPatterns(conversationId, "details");
            final int totalDeleted = deleteKeysByPatterns(patterns, logPrefix);

            final Cache cache = cacheManager.getCache(CONVERSATION_DETAILS_CACHE);
            if (cache != null) {
                cache.evict(conversationId.toString());
            }

            log.info("{}: удалено {} ключей для беседы {}", logPrefix, totalDeleted, conversationId);

        } catch (Exception e) {
            log.error("{}: ошибка при инвалидации деталей беседы {}", logPrefix, conversationId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictConversationMessages(final UUID conversationId) {
        final String logPrefix = "КЕШ_СООБЩЕНИЯ_БЕСЕДЫ_ИНВАЛИДАЦИЯ";

        if (conversationId == null) {
            log.warn("{}: conversationId не может быть null", logPrefix);
            return;
        }

        log.info("{}: инвалидация сообщений беседы {}", logPrefix, conversationId);

        try {
            final String[] patterns = createConversationPatterns(conversationId, "messages");
            final int totalDeleted = deleteKeysByPatterns(patterns, logPrefix);

            log.info("{}: кэш сообщений беседы {} успешно инвалидирован, удалено ключей: {}",
                    logPrefix, conversationId, totalDeleted);

        } catch (Exception e) {
            log.error("{}: ошибка при инвалидации сообщений беседы {}", logPrefix, conversationId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictUserConversations(final UUID userId) {
        final String logPrefix = "КЕШ_БЕСЕДЫ_ПОЛЬЗОВАТЕЛЯ_ИНВАЛИДАЦИЯ";

        if (userId == null) {
            log.warn("{}: userId не может быть null", logPrefix);
            return;
        }

        log.info("{}: инвалидация бесед пользователя {}", logPrefix, userId);

        try {
            final String[] patterns = createUserPatterns(userId, true);
            final int totalDeleted = deleteKeysByPatterns(patterns, logPrefix);

            log.info("{}: кэш бесед пользователя {} успешно инвалидирован, удалено ключей: {}",
                    logPrefix, userId, totalDeleted);

        } catch (Exception e) {
            log.error("{}: ошибка при инвалидации бесед пользователя {}", logPrefix, userId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictMessage(final UUID messageId) {
        final String logPrefix = "КЕШ_СООБЩЕНИЕ_ИНВАЛИДАЦИЯ";

        if (messageId == null) {
            log.warn("{}: messageId не может быть null", logPrefix);
            return;
        }

        log.debug("{}: инвалидация сообщения {}", logPrefix, messageId);

        try {
            final Cache cache = cacheManager.getCache(MESSAGE_CACHE);
            if (cache != null) {
                cache.evict(messageId.toString());
            }

            final String messageIdStr = messageId.toString();
            final String messageIdPattern = ANY_PREFIX + messageIdStr + ANY_PREFIX;
            final String messagePrefixPattern = ANY_PREFIX + MESSAGE_PREFIX + messageIdStr + ANY_PREFIX;

            final String[] patterns = {
                    MESSAGE_CACHE + CACHE_KEY_SEPARATOR + messagePrefixPattern,
                    MESSAGE_CACHE + CACHE_KEY_SEPARATOR + messageIdPattern,
                    MESSAGE_CACHE + ANY_PREFIX + messageIdStr + ANY_PREFIX,
                    "message" + CACHE_KEY_SEPARATOR + messageIdPattern,
                    "message" + ANY_PREFIX + messageIdStr + ANY_PREFIX,
                    ANY_PREFIX + "message" + ANY_PREFIX + messageIdStr + ANY_PREFIX,
                    ANY_PREFIX + CACHE_KEY_SEPARATOR + ANY_PREFIX + messageIdStr + ANY_PREFIX,
                    ANY_PREFIX + "Message" + ANY_PREFIX + messageIdStr + ANY_PREFIX
            };

            final int totalDeleted = deleteKeysByPatterns(patterns, logPrefix);

            log.debug("{}: кэш сообщения {} успешно инвалидирован, удалено ключей: {}",
                    logPrefix, messageId, totalDeleted);

        } catch (Exception e) {
            log.error("{}: ошибка при инвалидации сообщения {}", logPrefix, messageId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictConversationBetweenUsers(final UUID user1Id, final UUID user2Id) {
        final String logPrefix = "КЕШ_БЕСЕДА_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_ИНВАЛИДАЦИЯ";

        log.info("{}: инвалидация беседы между {} и {}", logPrefix, user1Id, user2Id);

        try {
            evictUserConversations(user1Id);
            evictUserConversations(user2Id);

            final String userId1Pattern = ANY_PREFIX + user1Id + ANY_PREFIX;
            final String userId2Pattern = ANY_PREFIX + user2Id + ANY_PREFIX;
            final String user1PrefixPattern = ANY_PREFIX + USER_PREFIX + user1Id + ANY_PREFIX;
            final String user2PrefixPattern = ANY_PREFIX + USER_PREFIX + user2Id + ANY_PREFIX;

            final String[] patterns = {
                    ANY_PREFIX + CACHE_KEY_SEPARATOR + user1PrefixPattern + ANY_PREFIX + user2PrefixPattern + ANY_PREFIX,
                    ANY_PREFIX + CACHE_KEY_SEPARATOR + user2PrefixPattern + ANY_PREFIX + user1PrefixPattern + ANY_PREFIX,
                    ANY_PREFIX + CACHE_KEY_SEPARATOR + userId1Pattern + ANY_PREFIX + userId2Pattern + ANY_PREFIX,
                    ANY_PREFIX + CACHE_KEY_SEPARATOR + userId2Pattern + ANY_PREFIX + userId1Pattern + ANY_PREFIX,
                    ANY_PREFIX + "user1:" + user1Id + ANY_PREFIX + "user2:" + user2Id + ANY_PREFIX,
                    ANY_PREFIX + "user1:" + user2Id + ANY_PREFIX + "user2:" + user1Id + ANY_PREFIX
            };

            final int totalDeleted = deleteKeysByPatterns(patterns, logPrefix);

            log.info("{}: кэш беседы между пользователями {} и {} успешно инвалидирован",
                    logPrefix, user1Id, user2Id);

        } catch (Exception e) {
            log.error("{}: ошибка при инвалидации беседы между пользователями", logPrefix, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictFirstPages() {
        final String logPrefix = "КЕШ_ПЕРВЫЕ_СТРАНИЦЫ_ИНВАЛИДАЦИЯ";

        log.info("{}: инвалидация первых страниц бесед", logPrefix);

        try {
            int totalDeleted = 0;

            for (int page = 0; page < PAGES_TO_INVALIDATE; page++) {
                final String pageSuffix = ANY_PREFIX + PAGE_PREFIX + page + ANY_PREFIX;
                final String[] patterns = {
                        USER_CONVERSATIONS_CACHE + CACHE_KEY_SEPARATOR + ANY_PREFIX + pageSuffix,
                        USER_CONVERSATIONS_CACHE + ANY_PREFIX + pageSuffix,
                        "userConversations" + CACHE_KEY_SEPARATOR + ANY_PREFIX + pageSuffix,
                        "userConversations" + ANY_PREFIX + pageSuffix,
                        ANY_PREFIX + "userConversations" + ANY_PREFIX + pageSuffix,
                        ANY_PREFIX + "detailed" + ANY_PREFIX + pageSuffix
                };
                totalDeleted += deleteKeysByPatterns(patterns, logPrefix);
            }

            log.info("{}: удалено {} ключей первых страниц бесед", logPrefix, totalDeleted);

        } catch (Exception e) {
            log.error("{}: ошибка при инвалидации первых страниц", logPrefix, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Async("cacheTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void evictAllMessagingCache() {
        final String logPrefix = "КЕШ_ПОЛНАЯ_ИНВАЛИДАЦИЯ_МЕССЕНДЖЕР";

        log.warn("{}: полная инвалидация кэша мессенджера", logPrefix);

        try {
            final String[] cacheNames = {
                    CONVERSATION_DETAILS_CACHE,
                    USER_CONVERSATIONS_CACHE,
                    CONVERSATION_MESSAGES_CACHE,
                    MESSAGE_CACHE
            };

            int clearedCaches = 0;
            int totalDeleted = 0;

            for (final String cacheName : cacheNames) {
                final Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    clearedCaches++;
                }

                final String[] patterns = {
                        cacheName + CACHE_KEY_SEPARATOR + ANY_PREFIX,
                        cacheName + ANY_PREFIX,
                        ANY_PREFIX + cacheName + ANY_PREFIX
                };
                totalDeleted += deleteKeysByPatterns(patterns, logPrefix);
            }

            log.warn("{}: полная инвалидация завершена, очищено Spring кэшей: {}, удалено Redis ключей: {}",
                    logPrefix, clearedCaches, totalDeleted);

        } catch (Exception e) {
            log.error("{}: ошибка при полной инвалидации кэша", logPrefix, e);
        }
    }
}