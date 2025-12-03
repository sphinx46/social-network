package ru.cs.vsu.social_network.messaging_service.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import ru.cs.vsu.social_network.messaging_service.service.serviceImpl.cache.MessagingCacheServiceImpl;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingCacheServiceImplTest {

    private static final UUID TEST_CONVERSATION_ID = TestDataFactory.TEST_CONVERSATION_ID;
    private static final UUID TEST_MESSAGE_ID = TestDataFactory.TEST_MESSAGE_ID;
    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;
    private static final UUID TEST_USER2_ID = TestDataFactory.TEST_USER2_ID;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private Cache conversationDetailsCache;

    @Mock
    private Cache userConversationsCache;

    @Mock
    private Cache conversationMessagesCache;

    @Mock
    private Cache messageCache;

    @InjectMocks
    private MessagingCacheServiceImpl messagingCacheService;

    @Nested
    @DisplayName("Публичные методы - тесты с использованием моков CacheManager")
    class PublicMethodsTests {

        @Test
        @DisplayName("Инвалидация деталей беседы - успешно")
        void evictConversationDetails_whenConversationExists_shouldDeleteKeys() {
            when(cacheManager.getCache("conversationDetails")).thenReturn(conversationDetailsCache);

            Set<String> keys = TestDataFactory.createRedisKeysForConversation(TEST_CONVERSATION_ID);
            when(redisTemplate.keys(anyString())).thenReturn(keys);
            when(redisTemplate.delete(anySet())).thenReturn(4L);

            messagingCacheService.evictConversationDetails(TEST_CONVERSATION_ID);

            verify(redisTemplate, atLeastOnce()).keys(anyString());
            verify(redisTemplate, atLeastOnce()).delete(anySet());
            verify(conversationDetailsCache).evict(TEST_CONVERSATION_ID.toString());
        }

        @Test
        @DisplayName("Инвалидация деталей беседы - ключи не найдены")
        void evictConversationDetails_whenNoKeysFound_shouldHandleGracefully() {
            when(cacheManager.getCache("conversationDetails")).thenReturn(conversationDetailsCache);

            when(redisTemplate.keys(anyString())).thenReturn(null);

            messagingCacheService.evictConversationDetails(TEST_CONVERSATION_ID);

            verify(redisTemplate, atLeastOnce()).keys(anyString());
            verify(redisTemplate, never()).delete(anySet());
            verify(conversationDetailsCache).evict(TEST_CONVERSATION_ID.toString());
        }

        @Test
        @DisplayName("Инвалидация сообщений беседы - успешно")
        void evictConversationMessages_whenConversationExists_shouldDeleteKeys() {
            Set<String> keys = TestDataFactory.createRedisKeysForConversation(TEST_CONVERSATION_ID);
            when(redisTemplate.keys(anyString())).thenReturn(keys);
            when(redisTemplate.delete(anySet())).thenReturn(4L);

            messagingCacheService.evictConversationMessages(TEST_CONVERSATION_ID);

            verify(redisTemplate, atLeastOnce()).keys(anyString());
            verify(redisTemplate, atLeastOnce()).delete(anySet());
        }

        @Test
        @DisplayName("Инвалидация бесед пользователя - успешно")
        void evictUserConversations_whenUserExists_shouldDeleteKeys() {
            Set<String> keys = TestDataFactory.createRedisKeysForUser(TEST_USER_ID);
            when(redisTemplate.keys(anyString())).thenReturn(keys);
            when(redisTemplate.delete(anySet())).thenReturn(4L);

            messagingCacheService.evictUserConversations(TEST_USER_ID);

            verify(redisTemplate, atLeastOnce()).keys(anyString());
            verify(redisTemplate, atLeastOnce()).delete(anySet());
        }

        @Test
        @DisplayName("Инвалидация сообщения - успешно")
        void evictMessage_whenMessageExists_shouldDeleteKeys() {
            when(cacheManager.getCache("message")).thenReturn(messageCache);

            Set<String> keys = TestDataFactory.createRedisKeysForMessage(TEST_MESSAGE_ID);
            when(redisTemplate.keys(anyString())).thenReturn(keys);
            when(redisTemplate.delete(anySet())).thenReturn(3L);

            messagingCacheService.evictMessage(TEST_MESSAGE_ID);

            verify(redisTemplate, atLeastOnce()).keys(anyString());
            verify(redisTemplate, atLeastOnce()).delete(anySet());
            verify(messageCache).evict(TEST_MESSAGE_ID.toString());
        }

        @Test
        @DisplayName("Инвалидация беседы между пользователями - успешно")
        void evictConversationBetweenUsers_whenUsersExist_shouldDeleteKeys() {
            Set<String> user1Keys = TestDataFactory.createRedisKeysForUser(TEST_USER_ID);
            Set<String> user2Keys = TestDataFactory.createRedisKeysForUser(TEST_USER2_ID);
            Set<String> combinedKeys = Set.of(
                    "userConversations::user:" + TEST_USER_ID + ":user:" + TEST_USER2_ID,
                    "userConversations::user:" + TEST_USER2_ID + ":user:" + TEST_USER_ID
            );

            when(redisTemplate.keys(anyString()))
                    .thenReturn(user1Keys)
                    .thenReturn(user2Keys)
                    .thenReturn(combinedKeys);

            when(redisTemplate.delete(anySet()))
                    .thenReturn(4L)
                    .thenReturn(4L)
                    .thenReturn(2L);

            messagingCacheService.evictConversationBetweenUsers(TEST_USER_ID, TEST_USER2_ID);

            verify(redisTemplate, atLeast(3)).keys(anyString());
            verify(redisTemplate, atLeast(3)).delete(anySet());
        }

        @Test
        @DisplayName("Инвалидация первых страниц - успешно")
        void evictFirstPages_whenCalled_shouldDeleteFirstPages() {
            Set<String> keys = TestDataFactory.createRedisKeysForFirstPages();
            when(redisTemplate.keys(anyString())).thenReturn(keys);
            when(redisTemplate.delete(anySet())).thenReturn(4L);

            messagingCacheService.evictFirstPages();

            verify(redisTemplate, atLeastOnce()).keys(anyString());
            verify(redisTemplate, atLeastOnce()).delete(anySet());
        }

        @Test
        @DisplayName("Полная инвалидация кэша мессенджера - успешно")
        void evictAllMessagingCache_whenCalled_shouldClearAllCaches() {
            when(cacheManager.getCache("conversationDetails")).thenReturn(conversationDetailsCache);
            when(cacheManager.getCache("userConversations")).thenReturn(userConversationsCache);
            when(cacheManager.getCache("conversationMessages")).thenReturn(conversationMessagesCache);
            when(cacheManager.getCache("message")).thenReturn(messageCache);

            Set<String> keys = Set.of("key1", "key2", "key3");
            when(redisTemplate.keys(anyString())).thenReturn(keys);
            when(redisTemplate.delete(anySet())).thenReturn(3L);

            messagingCacheService.evictAllMessagingCache();

            verify(conversationDetailsCache).clear();
            verify(userConversationsCache).clear();
            verify(conversationMessagesCache).clear();
            verify(messageCache).clear();

            verify(redisTemplate, atLeast(4)).keys(anyString());
            verify(redisTemplate, atLeast(4)).delete(anySet());
        }

        @Test
        @DisplayName("Полная инвалидация кэша мессенджера - кэш не найден")
        void evictAllMessagingCache_whenCacheNotFound_shouldHandleGracefully() {
            when(cacheManager.getCache("conversationDetails")).thenReturn(null);
            when(cacheManager.getCache("userConversations")).thenReturn(null);
            when(cacheManager.getCache("conversationMessages")).thenReturn(null);
            when(cacheManager.getCache("message")).thenReturn(null);

            Set<String> keys = Set.of("key1", "key2");
            when(redisTemplate.keys(anyString())).thenReturn(keys);
            when(redisTemplate.delete(anySet())).thenReturn(2L);

            messagingCacheService.evictAllMessagingCache();

            verify(cacheManager, times(4)).getCache(anyString());
            verify(redisTemplate, atLeast(4)).keys(anyString());
            verify(redisTemplate, atLeast(4)).delete(anySet());
        }

        @Test
        @DisplayName("Интеграционный тест - полный цикл инвалидации")
        void integrationTest_whenMultipleOperations_shouldWorkCorrectly() {
            UUID conversationId1 = UUID.randomUUID();
            UUID conversationId2 = UUID.randomUUID();
            UUID messageId1 = UUID.randomUUID();
            UUID messageId2 = UUID.randomUUID();
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            when(cacheManager.getCache("conversationDetails")).thenReturn(conversationDetailsCache);
            when(cacheManager.getCache("message")).thenReturn(messageCache);

            Set<String> keys = Set.of("key1", "key2");
            when(redisTemplate.keys(anyString())).thenReturn(keys);
            when(redisTemplate.delete(anySet())).thenReturn(2L);

            messagingCacheService.evictConversationDetails(conversationId1);
            messagingCacheService.evictConversationMessages(conversationId2);
            messagingCacheService.evictMessage(messageId1);
            messagingCacheService.evictMessage(messageId2);
            messagingCacheService.evictUserConversations(userId1);
            messagingCacheService.evictUserConversations(userId2);

            verify(redisTemplate, atLeast(6)).keys(anyString());
            verify(redisTemplate, atLeast(6)).delete(anySet());
            verify(conversationDetailsCache).evict(conversationId1.toString());
            verify(messageCache, times(2)).evict(anyString());
        }
    }

    @Nested
    @DisplayName("Проверка null параметров - тесты без моков CacheManager")
    class NullParameterTests {

        @Test
        @DisplayName("Инвалидация деталей беседы - null conversationId")
        void evictConversationDetails_whenConversationIdIsNull_shouldLogWarning() {
            messagingCacheService.evictConversationDetails(null);

            verify(redisTemplate, never()).keys(anyString());
            verify(redisTemplate, never()).delete(anySet());
            verify(cacheManager, never()).getCache(anyString());
        }

        @Test
        @DisplayName("Инвалидация сообщений беседы - null conversationId")
        void evictConversationMessages_whenConversationIdIsNull_shouldLogWarning() {
            messagingCacheService.evictConversationMessages(null);

            verify(redisTemplate, never()).keys(anyString());
            verify(redisTemplate, never()).delete(anySet());
            verify(cacheManager, never()).getCache(anyString());
        }

        @Test
        @DisplayName("Инвалидация бесед пользователя - null userId")
        void evictUserConversations_whenUserIdIsNull_shouldLogWarning() {
            messagingCacheService.evictUserConversations(null);

            verify(redisTemplate, never()).keys(anyString());
            verify(redisTemplate, never()).delete(anySet());
            verify(cacheManager, never()).getCache(anyString());
        }

        @Test
        @DisplayName("Инвалидация сообщения - null messageId")
        void evictMessage_whenMessageIdIsNull_shouldLogWarning() {
            messagingCacheService.evictMessage(null);

            verify(redisTemplate, never()).keys(anyString());
            verify(redisTemplate, never()).delete(anySet());
            verify(cacheManager, never()).getCache(anyString());
        }
    }

    @Nested
    @DisplayName("Приватные методы - тесты без использования моков CacheManager")
    class PrivateMethodsTests {

        @Test
        @DisplayName("Создание паттернов для пользователя - успешно")
        void createUserPatterns_whenCalled_shouldReturnCorrectPatterns() {
            try {
                Method method = MessagingCacheServiceImpl.class
                        .getDeclaredMethod("createUserPatterns", UUID.class, boolean.class);
                method.setAccessible(true);

                String[] patterns = (String[]) method.invoke(messagingCacheService, TEST_USER_ID, true);

                assertNotNull(patterns);
                assertTrue(patterns.length > 0);

                boolean containsUserIdPattern = false;
                for (String pattern : patterns) {
                    if (pattern.contains(TEST_USER_ID.toString())) {
                        containsUserIdPattern = true;
                        break;
                    }
                }
                assertTrue(containsUserIdPattern, "Patterns should contain userId");
            } catch (Exception e) {
                fail("Should not throw exception: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Создание паттернов для беседы - успешно")
        void createConversationPatterns_whenCalled_shouldReturnCorrectPatterns() {
            try {
                Method method = MessagingCacheServiceImpl.class
                        .getDeclaredMethod("createConversationPatterns", UUID.class, String.class);
                method.setAccessible(true);

                String[] patterns = (String[]) method.invoke(messagingCacheService, TEST_CONVERSATION_ID, "details");

                assertNotNull(patterns);
                assertTrue(patterns.length > 0);

                boolean containsConversationIdPattern = false;
                for (String pattern : patterns) {
                    if (pattern.contains(TEST_CONVERSATION_ID.toString())) {
                        containsConversationIdPattern = true;
                        break;
                    }
                }
                assertTrue(containsConversationIdPattern, "Patterns should contain conversationId");
            } catch (Exception e) {
                fail("Should not throw exception: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Удаление ключей по паттернам - успешно")
        void deleteKeysByPatterns_whenPatternsProvided_shouldDeleteKeys() {
            try {
                Method method = MessagingCacheServiceImpl.class
                        .getDeclaredMethod("deleteKeysByPatterns", String[].class, String.class);
                method.setAccessible(true);

                String[] patterns = {"userConversations::user:" + TEST_USER_ID + ":*", "userConversations::*" + TEST_USER_ID + "*"};
                Set<String> keys1 = Set.of("userConversations::user:" + TEST_USER_ID + ":page:0");
                Set<String> keys2 = Set.of("userConversations::" + TEST_USER_ID + ":detailed:true");

                when(redisTemplate.keys(patterns[0])).thenReturn(keys1);
                when(redisTemplate.keys(patterns[1])).thenReturn(keys2);
                when(redisTemplate.delete(keys1)).thenReturn(1L);
                when(redisTemplate.delete(keys2)).thenReturn(1L);

                int deleted = (int) method.invoke(messagingCacheService, patterns, "TEST_PREFIX");

                assertEquals(2, deleted, "Should have deleted 2 keys");
            } catch (Exception e) {
                fail("Should not throw exception: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Удаление ключей по паттернам - пустые ключи")
        void deleteKeysByPatterns_whenNoKeysFound_shouldReturnZero() {
            try {
                Method method = MessagingCacheServiceImpl.class
                        .getDeclaredMethod("deleteKeysByPatterns", String[].class, String.class);
                method.setAccessible(true);

                String[] patterns = {"userConversations::user:" + TEST_USER_ID + ":*"};

                when(redisTemplate.keys(patterns[0])).thenReturn(null);

                int deleted = (int) method.invoke(messagingCacheService, patterns, "TEST_PREFIX");

                assertEquals(0, deleted, "Should have deleted 0 keys");
            } catch (Exception e) {
                fail("Should not throw exception: " + e.getMessage());
            }
        }
    }
}