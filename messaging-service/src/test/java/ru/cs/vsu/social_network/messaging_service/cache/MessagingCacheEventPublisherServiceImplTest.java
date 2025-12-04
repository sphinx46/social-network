package ru.cs.vsu.social_network.messaging_service.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;
import ru.cs.vsu.social_network.messaging_service.event.GenericMessagingCacheEvent;
import ru.cs.vsu.social_network.messaging_service.service.serviceImpl.cache.CacheEventFallbackServiceImpl;
import ru.cs.vsu.social_network.messaging_service.service.serviceImpl.cache.MessagingCacheEventPublisherServiceImpl;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.messaging_service.utils.factory.cache.MessagingCacheEventFactory;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingCacheEventPublisherServiceImplTest {

    private static final UUID TEST_CONVERSATION_ID = TestDataFactory.TEST_CONVERSATION_ID;
    private static final UUID TEST_MESSAGE_ID = TestDataFactory.TEST_MESSAGE_ID;
    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;
    private static final UUID TEST_USER2_ID = TestDataFactory.TEST_USER2_ID;

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private MessagingCacheEventFactory cacheEventFactory;
    @Mock
    private CacheEventFallbackServiceImpl cacheEventFallbackService;

    @InjectMocks
    private MessagingCacheEventPublisherServiceImpl cacheEventPublisherService;

    @Test
    @DisplayName("Публикация события создания сообщения - успешно")
    void publishMessageCreated_whenRequestIsValid_shouldPublishEvent() {
        GenericMessagingCacheEvent cacheEvent = TestDataFactory.createCacheEvent(
                CacheEventType.MESSAGE_CREATED, TEST_CONVERSATION_ID);

        when(cacheEventFactory.createMessageEvent(
                eq(CacheEventType.MESSAGE_CREATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID)))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishMessageCreated(
                new Object(), new Object(), TEST_CONVERSATION_ID,
                TEST_MESSAGE_ID, TEST_USER_ID, TEST_USER2_ID);

        verify(cacheEventFactory).createMessageEvent(
                eq(CacheEventType.MESSAGE_CREATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID));
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события обновления сообщения - успешно (Message entity)")
    void publishMessageUpdated_whenMessageEntity_shouldExtractIds() {
        Message message = TestDataFactory.createMessageEntity();
        GenericMessagingCacheEvent cacheEvent = TestDataFactory.createCacheEvent(
                CacheEventType.MESSAGE_UPDATED, TEST_CONVERSATION_ID);

        when(cacheEventFactory.createMessageEvent(
                eq(CacheEventType.MESSAGE_UPDATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID)))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishMessageUpdated(
                new Object(), message, TEST_CONVERSATION_ID, TEST_MESSAGE_ID);

        verify(cacheEventFactory).createMessageEvent(
                eq(CacheEventType.MESSAGE_UPDATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID));
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события обновления сообщения - успешно (MessageResponse)")
    void publishMessageUpdated_whenMessageResponse_shouldExtractIds() {
        MessageResponse messageResponse = TestDataFactory.createMessageResponse();
        GenericMessagingCacheEvent cacheEvent = TestDataFactory.createCacheEvent(
                CacheEventType.MESSAGE_UPDATED, TEST_CONVERSATION_ID);

        when(cacheEventFactory.createMessageEvent(
                eq(CacheEventType.MESSAGE_UPDATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID)))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishMessageUpdated(
                new Object(), messageResponse, TEST_CONVERSATION_ID, TEST_MESSAGE_ID);

        verify(cacheEventFactory).createMessageEvent(
                eq(CacheEventType.MESSAGE_UPDATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID));
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события удаления сообщения - успешно")
    void publishMessageDeleted_whenRequestIsValid_shouldPublishEvent() {
        Message message = TestDataFactory.createMessageEntity();
        GenericMessagingCacheEvent cacheEvent = TestDataFactory.createCacheEvent(
                CacheEventType.MESSAGE_DELETED, TEST_CONVERSATION_ID);

        when(cacheEventFactory.createMessageEvent(
                eq(CacheEventType.MESSAGE_DELETED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID)))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishMessageDeleted(
                new Object(), message, TEST_CONVERSATION_ID, TEST_MESSAGE_ID);

        verify(cacheEventFactory).createMessageEvent(
                eq(CacheEventType.MESSAGE_DELETED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID));
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события создания беседы - успешно")
    void publishConversationCreated_whenRequestIsValid_shouldPublishEvent() {
        GenericMessagingCacheEvent cacheEvent = TestDataFactory.createCacheEvent(
                CacheEventType.CONVERSATION_CREATED, TEST_CONVERSATION_ID);

        when(cacheEventFactory.createConversationEvent(
                eq(CacheEventType.CONVERSATION_CREATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_USER_ID), eq(TEST_USER2_ID)))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishConversationCreated(
                new Object(), new Object(), TEST_CONVERSATION_ID, TEST_USER_ID, TEST_USER2_ID);

        verify(cacheEventFactory).createConversationEvent(
                eq(CacheEventType.CONVERSATION_CREATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_USER_ID), eq(TEST_USER2_ID));
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события прочтения сообщений - успешно")
    void publishMessagesRead_whenRequestIsValid_shouldPublishEvent() {
        GenericMessagingCacheEvent cacheEvent = TestDataFactory.createCacheEvent(
                CacheEventType.MESSAGES_READ, TEST_CONVERSATION_ID);

        when(cacheEventFactory.createMessageStatusEvent(
                eq(CacheEventType.MESSAGES_READ), any(), any(), eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID)))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishMessagesRead(
                new Object(), new Object(), TEST_CONVERSATION_ID, TEST_USER_ID);

        verify(cacheEventFactory).createMessageStatusEvent(
                eq(CacheEventType.MESSAGES_READ), any(), any(), eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID));
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события загрузки изображения сообщения - успешно")
    void publishMessageImageUploaded_whenRequestIsValid_shouldPublishEvent() {
        Message message = TestDataFactory.createMessageEntity();
        GenericMessagingCacheEvent cacheEvent = TestDataFactory.createCacheEvent(
                CacheEventType.MESSAGE_IMAGE_UPLOADED, TEST_CONVERSATION_ID);

        when(cacheEventFactory.createMessageEvent(
                eq(CacheEventType.MESSAGE_IMAGE_UPLOADED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID)))
                .thenReturn(cacheEvent);
        doNothing().when(eventPublisher).publishEvent(cacheEvent);

        cacheEventPublisherService.publishMessageImageUploaded(
                new Object(), message, TEST_CONVERSATION_ID, TEST_MESSAGE_ID);

        verify(cacheEventFactory).createMessageEvent(
                eq(CacheEventType.MESSAGE_IMAGE_UPLOADED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID));
        verify(eventPublisher).publishEvent(cacheEvent);
    }

    @Test
    @DisplayName("Публикация события - ошибка публикации")
    void publishEventSafely_whenPublisherThrowsException_shouldRegisterFallback() {
        GenericMessagingCacheEvent cacheEvent = TestDataFactory.createCacheEvent(
                CacheEventType.MESSAGE_CREATED, TEST_CONVERSATION_ID);
        RuntimeException exception = new RuntimeException("Publishing failed");

        when(cacheEventFactory.createMessageEvent(
                eq(CacheEventType.MESSAGE_CREATED), any(), any(), eq(TEST_CONVERSATION_ID),
                eq(TEST_MESSAGE_ID), eq(TEST_USER_ID), eq(TEST_USER2_ID)))
                .thenReturn(cacheEvent);
        doThrow(exception).when(eventPublisher).publishEvent(cacheEvent);
        doNothing().when(cacheEventFallbackService).registerPendingInvalidation(
                eq(TEST_CONVERSATION_ID), eq(CacheEventType.MESSAGE_CREATED));

        cacheEventPublisherService.publishMessageCreated(
                new Object(), new Object(), TEST_CONVERSATION_ID,
                TEST_MESSAGE_ID, TEST_USER_ID, TEST_USER2_ID);

        verify(eventPublisher).publishEvent(cacheEvent);
        verify(cacheEventFallbackService).registerPendingInvalidation(
                eq(TEST_CONVERSATION_ID), eq(CacheEventType.MESSAGE_CREATED));
    }

    @Test
    @DisplayName("Извлечение senderId из Message entity - успешно")
    void extractSenderId_whenMessageEntity_shouldReturnSenderId() {
        Message message = TestDataFactory.createMessageEntity();

        try {
            Method method = MessagingCacheEventPublisherServiceImpl.class
                    .getDeclaredMethod("extractSenderId", Object.class);
            method.setAccessible(true);

            UUID result = (UUID) method.invoke(cacheEventPublisherService, message);

            assertNotNull(result);
            assertEquals(TEST_USER_ID, result);
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Извлечение receiverId из MessageResponse - успешно")
    void extractReceiverId_whenMessageResponse_shouldReturnReceiverId() {
        MessageResponse messageResponse = TestDataFactory.createMessageResponse();

        try {
            Method method = MessagingCacheEventPublisherServiceImpl.class
                    .getDeclaredMethod("extractReceiverId", Object.class);
            method.setAccessible(true);

            UUID result = (UUID) method.invoke(cacheEventPublisherService, messageResponse);

            assertNotNull(result);
            assertEquals(TEST_USER2_ID, result);
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Извлечение senderId - неизвестный тип")
    void extractSenderId_whenUnknownType_shouldReturnNull() {
        try {
            Method method = MessagingCacheEventPublisherServiceImpl.class
                    .getDeclaredMethod("extractSenderId", Object.class);
            method.setAccessible(true);

            UUID result = (UUID) method.invoke(cacheEventPublisherService, new Object());

            assertNull(result);
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Безопасное выполнение действия - успешно")
    void executeActionSafely_whenActionValid_shouldExecute() {
        boolean[] executed = {false};
        Runnable action = () -> executed[0] = true;

        try {
            Method method = MessagingCacheEventPublisherServiceImpl.class
                    .getDeclaredMethod("executeActionSafely", Runnable.class);
            method.setAccessible(true);

            method.invoke(cacheEventPublisherService, action);

            assertTrue(executed[0], "Action should have been executed");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Безопасное выполнение действия - ошибка")
    void executeActionSafely_whenActionThrowsException_shouldHandleGracefully() {
        Runnable action = () -> {
            throw new RuntimeException("Test exception");
        };

        try {
            Method method = MessagingCacheEventPublisherServiceImpl.class
                    .getDeclaredMethod("executeActionSafely", Runnable.class);
            method.setAccessible(true);

            method.invoke(cacheEventPublisherService, action);
        } catch (Exception e) {
            fail("Exception should be handled inside executeActionSafely method: " + e.getMessage());
        }
    }
}