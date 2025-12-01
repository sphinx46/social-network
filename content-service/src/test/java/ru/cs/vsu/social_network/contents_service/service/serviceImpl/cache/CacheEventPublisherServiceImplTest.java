package ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;
import ru.cs.vsu.social_network.contents_service.events.cache.GenericCacheEvent;
import ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache.CacheEventFallbackServiceImpl;
import ru.cs.vsu.social_network.contents_service.service.serviceImpl.cache.CacheEventPublisherServiceImpl;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.cache.CacheEventFactory;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link CacheEventPublisherServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CacheEventPublisherServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID COMMENT_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    private static final UUID LIKE_ID = UUID.fromString("5ab3c6d7-ec6f-49ad-95ac-6c752ad8172e");

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private CacheEventFactory cacheEventFactory;
    @Mock
    private CacheEventFallbackServiceImpl cacheEventFallbackService;

    @InjectMocks
    private CacheEventPublisherServiceImpl cacheEventPublisherService;

    @Test
    @DisplayName("Публикация события создания поста - успешно")
    void publishPostCreated_whenRequestIsValid_shouldPublishEvent() {
        GenericCacheEvent cacheEvent = TestDataFactory.createPostCreatedEvent(POST_ID);

        when(cacheEventFactory.createPostEvent(eq(CacheEventType.POST_CREATED), any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishPostCreated(new Object(), new Object(), USER_ID, POST_ID);

        verify(cacheEventFactory).createPostEvent(eq(CacheEventType.POST_CREATED), any(), any(), any(), any());
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события обновления поста - успешно")
    void publishPostUpdated_whenRequestIsValid_shouldPublishEvent() {
        GenericCacheEvent cacheEvent = TestDataFactory.createPostUpdatedEvent(POST_ID);

        when(cacheEventFactory.createPostEvent(eq(CacheEventType.POST_UPDATED), any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishPostUpdated(new Object(), new Object(), USER_ID, POST_ID);

        verify(cacheEventFactory).createPostEvent(eq(CacheEventType.POST_UPDATED), any(), any(), any(), any());
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события создания комментария - успешно")
    void publishCommentCreated_whenRequestIsValid_shouldPublishEvent() {
        GenericCacheEvent cacheEvent = TestDataFactory.createCommentAddedEvent(POST_ID);

        when(cacheEventFactory.createCommentEvent(eq(CacheEventType.COMMENT_ADDED),
                any(), any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishCommentCreated(new Object(), new Object(), POST_ID, COMMENT_ID, USER_ID);

        verify(cacheEventFactory).createCommentEvent(eq(CacheEventType.COMMENT_ADDED),
                any(), any(), any(), any(), any());
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события обновления комментария - успешно")
    void publishCommentUpdated_whenRequestIsValid_shouldPublishEvent() {
        GenericCacheEvent cacheEvent = TestDataFactory.createCacheEvent(CacheEventType.COMMENT_UPDATED, POST_ID);

        when(cacheEventFactory.createCommentEvent(eq(CacheEventType.COMMENT_UPDATED),
                any(), any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishCommentUpdated(new Object(), new Object(), POST_ID, COMMENT_ID, USER_ID);

        verify(cacheEventFactory).createCommentEvent(eq(CacheEventType.COMMENT_UPDATED),
                any(), any(), any(), any(), any());
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события удаления комментария - успешно")
    void publishCommentDeleted_whenRequestIsValid_shouldPublishEvent() {
        GenericCacheEvent cacheEvent = TestDataFactory.createCacheEvent(CacheEventType.COMMENT_DELETED, POST_ID);

        when(cacheEventFactory.createCommentEvent(eq(CacheEventType.COMMENT_DELETED),
                any(), any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishCommentDeleted(new Object(), new Object(), POST_ID, COMMENT_ID, USER_ID);

        verify(cacheEventFactory).createCommentEvent(eq(CacheEventType.COMMENT_DELETED),
                any(), any(), any(), any(), any());
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события создания лайка поста - успешно")
    void publishPostLikeCreated_whenRequestIsValid_shouldPublishEvent() {
        GenericCacheEvent cacheEvent = TestDataFactory.createLikeAddedEvent(POST_ID);

        when(cacheEventFactory.createPostLikedEvent(eq(CacheEventType.LIKE_ADDED),
                any(), any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishPostLikeCreated(new Object(), new Object(),
                POST_ID, LIKE_ID, USER_ID);

        verify(cacheEventFactory).createPostLikedEvent(eq(CacheEventType.LIKE_ADDED),
                any(), any(), any(), any(), any());
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события удаления лайка поста - успешно")
    void publishPostLikeDeleted_whenRequestIsValid_shouldPublishEvent() {
        GenericCacheEvent cacheEvent = TestDataFactory.createCacheEvent(CacheEventType.LIKE_DELETED, POST_ID);

        when(cacheEventFactory.createPostLikedEvent(eq(CacheEventType.LIKE_DELETED),
                any(), any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishPostLikeDeleted(new Object(), new Object(), POST_ID, LIKE_ID, USER_ID);

        verify(cacheEventFactory).createPostLikedEvent(eq(CacheEventType.LIKE_DELETED),
                any(), any(), any(), any(), any());
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события - ошибка публикации")
    void publishEventSafely_whenPublisherThrowsException_shouldRegisterFallback() {
        GenericCacheEvent cacheEvent = TestDataFactory.createPostCreatedEvent(POST_ID);
        RuntimeException exception = new RuntimeException("Publishing failed");

        when(cacheEventFactory.createPostEvent(eq(CacheEventType.POST_CREATED),
                any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doThrow(exception).when(eventPublisher).publishEvent(cacheEvent);
        doNothing().when(cacheEventFallbackService).registerPendingInvalidation(eq(POST_ID),
                eq(CacheEventType.POST_CREATED));

        cacheEventPublisherService.publishPostCreated(new Object(), new Object(), USER_ID, POST_ID);

        verify(eventPublisher).publishEvent(cacheEvent);
        verify(cacheEventFallbackService).registerPendingInvalidation(eq(POST_ID), eq(CacheEventType.POST_CREATED));
    }

    @Test
    @DisplayName("Публикация события - транзакция не активна")
    void publishWithTransactionSync_whenTransactionNotActive_shouldExecuteImmediately() {
        GenericCacheEvent cacheEvent = TestDataFactory.createPostCreatedEvent(POST_ID);

        when(cacheEventFactory.createPostEvent(eq(CacheEventType.POST_CREATED), any(), any(), any(), any()))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);


        cacheEventPublisherService.publishPostCreated(new Object(), new Object(), USER_ID, POST_ID);

        verify(cacheEventFactory).createPostEvent(eq(CacheEventType.POST_CREATED), any(), any(), any(), any());
        verify(eventPublisher).publishEvent(cacheEvent);
    }
}