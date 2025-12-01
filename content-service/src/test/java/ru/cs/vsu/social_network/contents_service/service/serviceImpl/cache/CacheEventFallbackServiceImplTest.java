package ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;
import ru.cs.vsu.social_network.contents_service.service.cache.ContentCacheService;
import ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache.CacheEventFallbackServiceImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link CacheEventFallbackServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CacheEventFallbackServiceImplTest {

    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");

    @Mock
    private ContentCacheService contentCacheService;

    @InjectMocks
    private CacheEventFallbackServiceImpl cacheEventFallbackService;

    @Test
    @DisplayName("Регистрация отложенной инвалидации - успешно")
    void registerPendingInvalidation_whenValidParameters_shouldRegister() {
        cacheEventFallbackService.registerPendingInvalidation(POST_ID, CacheEventType.POST_CREATED);

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Регистрация отложенной инвалидации - null postId")
    void registerPendingInvalidation_whenPostIdIsNull_shouldLogWarning() {
        cacheEventFallbackService.registerPendingInvalidation(null, CacheEventType.POST_CREATED);

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Немедленная инвалидация - успешно")
    void executeImmediateInvalidation_whenPostIdValid_shouldExecuteInvalidation() {
        doNothing().when(contentCacheService).evictPostDetails(POST_ID);
        doNothing().when(contentCacheService).evictPostPages(POST_ID);

        cacheEventFallbackService.executeImmediateInvalidation(POST_ID);

        verify(contentCacheService, times(1)).evictPostDetails(POST_ID);
        verify(contentCacheService, times(1)).evictPostPages(POST_ID);
    }

    @Test
    @DisplayName("Немедленная инвалидация - null postId")
    void executeImmediateInvalidation_whenPostIdIsNull_shouldLogWarning() {
        cacheEventFallbackService.executeImmediateInvalidation(null);

        verify(contentCacheService, never()).evictPostDetails(any());
        verify(contentCacheService, never()).evictPostPages(any());
    }

    @Test
    @DisplayName("Обработка отложенных инвалидаций - пустая очередь")
    void processPendingInvalidations_whenQueueEmpty_shouldDoNothing() {
        cacheEventFallbackService.processPendingInvalidations();

        verify(contentCacheService, never()).evictPostDetails(any());
        verify(contentCacheService, never()).evictPostPages(any());
    }

    @Test
    @DisplayName("Обработка отложенных инвалидаций - успешная обработка")
    void processPendingInvalidations_whenQueueHasItems_shouldProcessAll() {
        UUID postId2 = UUID.randomUUID();
        cacheEventFallbackService.registerPendingInvalidation(POST_ID, CacheEventType.POST_CREATED);
        cacheEventFallbackService.registerPendingInvalidation(postId2, CacheEventType.POST_UPDATED);

        doNothing().when(contentCacheService).evictPostDetails(any());
        doNothing().when(contentCacheService).evictPostPages(any());

        cacheEventFallbackService.processPendingInvalidations();

        verify(contentCacheService, times(2)).evictPostDetails(any());
        verify(contentCacheService, times(2)).evictPostPages(any());

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Обработка отложенных инвалидаций - ошибка инвалидации")
    void processPendingInvalidations_whenInvalidationFails_shouldLogError() {
        UUID anotherPostId = UUID.randomUUID();
        cacheEventFallbackService.registerPendingInvalidation(POST_ID, CacheEventType.POST_CREATED);
        cacheEventFallbackService.registerPendingInvalidation(anotherPostId, CacheEventType.COMMENT_ADDED);

        doNothing().when(contentCacheService).evictPostDetails(POST_ID);
        doNothing().when(contentCacheService).evictPostPages(POST_ID);

        doThrow(new RuntimeException("Cache error"))
                .doThrow(new RuntimeException("Cache error"))
                .doThrow(new RuntimeException("Cache error"))
                .when(contentCacheService).evictPostDetails(anotherPostId);


        cacheEventFallbackService.processPendingInvalidations();

        verify(contentCacheService, times(4)).evictPostDetails(any());
        verify(contentCacheService, times(1)).evictPostPages(any());
    }

    @Test
    @DisplayName("Получение количества отложенных инвалидаций - успешно")
    void getPendingInvalidationsCount_whenQueueHasItems_shouldReturnCorrectCount() {
        assertEquals(0, cacheEventFallbackService.getPendingInvalidationsCount());

        cacheEventFallbackService.registerPendingInvalidation(POST_ID, CacheEventType.POST_CREATED);
        assertEquals(1, cacheEventFallbackService.getPendingInvalidationsCount());

        cacheEventFallbackService.registerPendingInvalidation(UUID.randomUUID(), CacheEventType.POST_UPDATED);
        assertEquals(2, cacheEventFallbackService.getPendingInvalidationsCount());
    }

    @Test
    @DisplayName("Выполнение инвалидации с повторными попытками - успех с первой попытки")
    void executeInvalidationWithRetry_whenFirstAttemptSuccess_shouldNotRetry() {
        doNothing().when(contentCacheService).evictPostDetails(POST_ID);
        doNothing().when(contentCacheService).evictPostPages(POST_ID);

        cacheEventFallbackService.executeImmediateInvalidation(POST_ID);

        verify(contentCacheService, times(1)).evictPostDetails(POST_ID);
        verify(contentCacheService, times(1)).evictPostPages(POST_ID);
    }

    @Test
    @DisplayName("Выполнение инвалидации с повторными попытками - успех со второй попытки")
    void executeInvalidationWithRetry_whenFirstAttemptFails_shouldRetry() {
        doThrow(new RuntimeException("First attempt failed"))
                .doNothing()
                .when(contentCacheService).evictPostDetails(POST_ID);

        doNothing().when(contentCacheService).evictPostPages(POST_ID);

        cacheEventFallbackService.executeImmediateInvalidation(POST_ID);

        verify(contentCacheService, times(2)).evictPostDetails(POST_ID);
        verify(contentCacheService, times(1)).evictPostPages(POST_ID);
    }

    @Test
    @DisplayName("Выполнение инвалидации с повторными попытками - все попытки провалены")
    void executeInvalidationWithRetry_whenAllAttemptsFail_shouldLogError() {
        RuntimeException exception = new RuntimeException("Cache unavailable");

        doThrow(exception)
                .doThrow(exception)
                .doThrow(exception)
                .when(contentCacheService).evictPostDetails(POST_ID);


        cacheEventFallbackService.executeImmediateInvalidation(POST_ID);

        verify(contentCacheService, times(3)).evictPostDetails(POST_ID);
        verify(contentCacheService, never()).evictPostPages(POST_ID);
    }
}