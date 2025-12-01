package ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache.ContentCacheServiceImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link ContentCacheServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ContentCacheServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");

    @Mock
    private CacheManager cacheManager;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private Cache postDetailsCache;
    @Mock
    private Cache userPageDetailsCache;
    @Mock
    private Cache allPageDetailsCache;

    @InjectMocks
    private ContentCacheServiceImpl contentCacheService;

    @Test
    @DisplayName("Инвалидация деталей поста - успешно")
    void evictPostDetails_whenPostExists_shouldDeleteKeys() {
        String pattern = "postDetails::post:" + POST_ID + ":*";
        Set<String> keys = new HashSet<>();
        keys.add("postDetails::post:" + POST_ID + ":comments:true");
        keys.add("postDetails::post:" + POST_ID + ":likes:false");

        when(redisTemplate.keys(pattern)).thenReturn(keys);

        contentCacheService.evictPostDetails(POST_ID);

        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(anyCollection());
    }

    @Test
    @DisplayName("Инвалидация деталей поста - ключи не найдены")
    void evictPostDetails_whenNoKeysFound_shouldLogWarning() {
        String pattern = "postDetails::post:" + POST_ID + ":*";

        when(redisTemplate.keys(pattern)).thenReturn(null);

        contentCacheService.evictPostDetails(POST_ID);

        verify(redisTemplate).keys(pattern);
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("Инвалидация деталей поста - null postId")
    void evictPostDetails_whenPostIdIsNull_shouldLogWarning() {
        contentCacheService.evictPostDetails(null);

        verify(redisTemplate, never()).keys(anyString());
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("Инвалидация страниц поста - успешно")
    void evictPostPages_whenPostExists_shouldDeleteFirstPages() {
        String userPagePattern1 = "userPageDetails::*page:0*";
        String userPagePattern2 = "userPageDetails::*page:1*";
        String allPagePattern1 = "allPageDetails::*page:0*";
        String allPagePattern2 = "allPageDetails::*page:1*";

        Set<String> userKeys1 = Set.of("userPageDetails::user:" + USER_ID + ":page:0");
        Set<String> userKeys2 = Set.of("userPageDetails::user:" + USER_ID + ":page:1");
        Set<String> allKeys1 = Set.of("allPageDetails::page:0");
        Set<String> allKeys2 = Set.of("allPageDetails::page:1");

        when(redisTemplate.keys(userPagePattern1)).thenReturn(userKeys1);
        when(redisTemplate.keys(userPagePattern2)).thenReturn(userKeys2);
        when(redisTemplate.keys(allPagePattern1)).thenReturn(allKeys1);
        when(redisTemplate.keys(allPagePattern2)).thenReturn(allKeys2);

        contentCacheService.evictPostPages(POST_ID);

        // Проверяем, что вызывается deleteFirstPages() (в нем 4 вызова keys)
        verify(redisTemplate, atLeast(1)).keys(anyString());
        verify(redisTemplate, atLeast(1)).delete(anyCollection());
    }

    @Test
    @DisplayName("Инвалидация страниц поста - null postId")
    void evictPostPages_whenPostIdIsNull_shouldLogWarning() {
        contentCacheService.evictPostPages(null);

        verify(redisTemplate, never()).keys(anyString());
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("Инвалидация первых страниц - успешно")
    void evictFirstPages_whenCalled_shouldDeleteFirstTwoPages() {
        String pattern1 = "userPageDetails::*page:0*";
        String pattern2 = "userPageDetails::*page:1*";
        String pattern3 = "allPageDetails::*page:0*";
        String pattern4 = "allPageDetails::*page:1*";

        Set<String> keys1 = Set.of("userPageDetails::page:0");
        Set<String> keys2 = Set.of("userPageDetails::page:1");
        Set<String> keys3 = Set.of("allPageDetails::page:0");
        Set<String> keys4 = Set.of("allPageDetails::page:1");

        when(redisTemplate.keys(pattern1)).thenReturn(keys1);
        when(redisTemplate.keys(pattern2)).thenReturn(keys2);
        when(redisTemplate.keys(pattern3)).thenReturn(keys3);
        when(redisTemplate.keys(pattern4)).thenReturn(keys4);

        contentCacheService.evictFirstPages();

        verify(redisTemplate).keys(pattern1);
        verify(redisTemplate).keys(pattern2);
        verify(redisTemplate).keys(pattern3);
        verify(redisTemplate).keys(pattern4);
        verify(redisTemplate, times(4)).delete(anyCollection());
    }

    @Test
    @DisplayName("Полная инвалидация кэша деталей постов - успешно")
    void evictAllPostDetailsCache_whenCalled_shouldClearAllCaches() {
        when(cacheManager.getCache("postDetails")).thenReturn(postDetailsCache);
        when(cacheManager.getCache("userPageDetails")).thenReturn(userPageDetailsCache);
        when(cacheManager.getCache("allPageDetails")).thenReturn(allPageDetailsCache);

        contentCacheService.evictAllPostDetailsCache();

        verify(postDetailsCache).clear();
        verify(userPageDetailsCache).clear();
        verify(allPageDetailsCache).clear();
    }

    @Test
    @DisplayName("Полная инвалидация кэша деталей постов - кэш не найден")
    void evictAllPostDetailsCache_whenCacheNotFound_shouldHandleGracefully() {
        when(cacheManager.getCache("postDetails")).thenReturn(null);
        when(cacheManager.getCache("userPageDetails")).thenReturn(null);
        when(cacheManager.getCache("allPageDetails")).thenReturn(null);

        contentCacheService.evictAllPostDetailsCache();

        verify(cacheManager, times(3)).getCache(anyString());
    }

    @Test
    @DisplayName("Инвалидация страниц пользователя - успешно")
    void evictUserPages_whenUserExists_shouldDeleteUserKeys() {
        String pattern = "userPageDetails::*user:" + USER_ID + ":*";
        Set<String> keys = Set.of(
                "userPageDetails::user:" + USER_ID + ":page:0",
                "userPageDetails::user:" + USER_ID + ":page:1"
        );

        when(redisTemplate.keys(pattern)).thenReturn(keys);

        contentCacheService.evictUserPages(USER_ID);

        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(anyCollection());
    }

    @Test
    @DisplayName("Инвалидация страниц пользователя - null userId")
    void evictUserPages_whenUserIdIsNull_shouldLogWarning() {
        contentCacheService.evictUserPages(null);

        verify(redisTemplate, never()).keys(anyString());
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("Инвалидация страниц пользователя - ключи не найдены")
    void evictUserPages_whenNoKeysFound_shouldHandleGracefully() {
        String pattern = "userPageDetails::*user:" + USER_ID + ":*";

        when(redisTemplate.keys(pattern)).thenReturn(null);

        contentCacheService.evictUserPages(USER_ID);

        verify(redisTemplate).keys(pattern);
        verify(redisTemplate, never()).delete(anyCollection());
    }

    @Test
    @DisplayName("Инвалидация деталей поста - ошибка Redis")
    void evictPostDetails_whenRedisError_shouldHandleException() {
        String pattern = "postDetails::post:" + POST_ID + ":*";
        Set<String> keys = Set.of("postDetails::post:" + POST_ID + ":key");

        when(redisTemplate.keys(pattern)).thenReturn(keys);
        doThrow(new RuntimeException("Redis connection error")).when(redisTemplate).delete(anyCollection());

        try {
            contentCacheService.evictPostDetails(POST_ID);
        } catch (Exception e) {
            throw new AssertionError("Exception should be handled inside evictPostDetails method", e);
        }

        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(anyCollection());
    }
}