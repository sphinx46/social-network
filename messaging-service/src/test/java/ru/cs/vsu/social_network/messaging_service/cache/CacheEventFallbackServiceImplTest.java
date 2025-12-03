package ru.cs.vsu.social_network.messaging_service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheService;
import ru.cs.vsu.social_network.messaging_service.service.serviceImpl.cache.CacheEventFallbackServiceImpl;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheEventFallbackServiceImplTest {

    private static final UUID TEST_CONVERSATION_ID = TestDataFactory.TEST_CONVERSATION_ID;
    private static final UUID TEST_MESSAGE_ID = TestDataFactory.TEST_MESSAGE_ID;
    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;

    @Mock
    private MessagingCacheService messagingCacheService;

    @InjectMocks
    private CacheEventFallbackServiceImpl cacheEventFallbackService;

    @BeforeEach
    void setUp() {
        cacheEventFallbackService.clearAllPendingInvalidations();
    }

    @Test
    @DisplayName("Регистрация отложенной инвалидации для беседы - успешно")
    void registerPendingInvalidation_whenValidParameters_shouldRegister() {
        cacheEventFallbackService.registerPendingInvalidation(TEST_CONVERSATION_ID, CacheEventType.MESSAGE_CREATED);

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Регистрация отложенной инвалидации для беседы - null conversationId")
    void registerPendingInvalidation_whenConversationIdIsNull_shouldLogWarning() {
        cacheEventFallbackService.registerPendingInvalidation(null, CacheEventType.MESSAGE_CREATED);

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Регистрация отложенной инвалидации для сообщения - успешно")
    void registerPendingInvalidationForMessage_whenValidParameters_shouldRegister() {
        cacheEventFallbackService.registerPendingInvalidationForMessage(TEST_MESSAGE_ID, CacheEventType.MESSAGE_UPDATED);

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Регистрация отложенной инвалидации для сообщения - null messageId")
    void registerPendingInvalidationForMessage_whenMessageIdIsNull_shouldLogWarning() {
        cacheEventFallbackService.registerPendingInvalidationForMessage(null, CacheEventType.MESSAGE_UPDATED);

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Регистрация отложенной инвалидации для пользователя - успешно")
    void registerPendingInvalidationForUser_whenValidParameters_shouldRegister() {
        cacheEventFallbackService.registerPendingInvalidationForUser(TEST_USER_ID, CacheEventType.MESSAGES_READ);

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Регистрация отложенной инвалидации для пользователя - null userId")
    void registerPendingInvalidationForUser_whenUserIdIsNull_shouldLogWarning() {
        cacheEventFallbackService.registerPendingInvalidationForUser(null, CacheEventType.MESSAGES_READ);

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Немедленная инвалидация - успешно")
    void executeImmediateInvalidation_whenConversationIdValid_shouldExecuteInvalidation() {
        doNothing().when(messagingCacheService).evictConversationDetails(TEST_CONVERSATION_ID);
        doNothing().when(messagingCacheService).evictConversationMessages(TEST_CONVERSATION_ID);
        doNothing().when(messagingCacheService).evictFirstPages();

        cacheEventFallbackService.executeImmediateInvalidation(TEST_CONVERSATION_ID);

        verify(messagingCacheService, times(1)).evictConversationDetails(TEST_CONVERSATION_ID);
        verify(messagingCacheService, times(1)).evictConversationMessages(TEST_CONVERSATION_ID);
        verify(messagingCacheService, times(1)).evictFirstPages();
    }

    @Test
    @DisplayName("Немедленная инвалидация - null conversationId")
    void executeImmediateInvalidation_whenConversationIdIsNull_shouldLogWarning() {
        cacheEventFallbackService.executeImmediateInvalidation(null);

        verify(messagingCacheService, never()).evictConversationDetails(any());
        verify(messagingCacheService, never()).evictConversationMessages(any());
        verify(messagingCacheService, never()).evictFirstPages();
    }

    @Test
    @DisplayName("Обработка отложенных инвалидаций бесед - пустая очередь")
    void processPendingConversationInvalidations_whenQueueEmpty_shouldDoNothing() {
        cacheEventFallbackService.processPendingConversationInvalidations();

        verify(messagingCacheService, never()).evictConversationDetails(any());
        verify(messagingCacheService, never()).evictConversationMessages(any());
        verify(messagingCacheService, never()).evictFirstPages();
    }

    @Test
    @DisplayName("Обработка отложенных инвалидаций бесед - успешная обработка")
    void processPendingConversationInvalidations_whenQueueHasItems_shouldProcessAll() {
        UUID conversationId2 = UUID.randomUUID();
        cacheEventFallbackService.registerPendingInvalidation(TEST_CONVERSATION_ID, CacheEventType.MESSAGE_CREATED);
        cacheEventFallbackService.registerPendingInvalidation(conversationId2, CacheEventType.MESSAGE_UPDATED);

        doNothing().when(messagingCacheService).evictConversationDetails(any());
        doNothing().when(messagingCacheService).evictConversationMessages(any());
        doNothing().when(messagingCacheService).evictFirstPages();

        cacheEventFallbackService.processPendingConversationInvalidations();

        verify(messagingCacheService, times(2)).evictConversationDetails(any());
        verify(messagingCacheService, times(2)).evictConversationMessages(any());
        verify(messagingCacheService, times(2)).evictFirstPages();

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Обработка отложенных инвалидаций сообщений - успешная обработка")
    void processPendingMessageInvalidations_whenQueueHasItems_shouldProcessAll() {
        UUID messageId2 = UUID.randomUUID();
        cacheEventFallbackService.registerPendingInvalidationForMessage(TEST_MESSAGE_ID, CacheEventType.MESSAGE_CREATED);
        cacheEventFallbackService.registerPendingInvalidationForMessage(messageId2, CacheEventType.MESSAGE_DELETED);

        doNothing().when(messagingCacheService).evictMessage(any());

        cacheEventFallbackService.processPendingMessageInvalidations();

        verify(messagingCacheService, times(2)).evictMessage(any());

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Обработка отложенных инвалидаций пользователей - успешная обработка")
    void processPendingUserInvalidations_whenQueueHasItems_shouldProcessAll() {
        UUID userId2 = UUID.randomUUID();
        cacheEventFallbackService.registerPendingInvalidationForUser(TEST_USER_ID, CacheEventType.MESSAGES_READ);
        cacheEventFallbackService.registerPendingInvalidationForUser(userId2, CacheEventType.CONVERSATION_CREATED);

        doNothing().when(messagingCacheService).evictUserConversations(any());
        doNothing().when(messagingCacheService).evictFirstPages();

        cacheEventFallbackService.processPendingUserInvalidations();

        verify(messagingCacheService, times(2)).evictUserConversations(any());
        verify(messagingCacheService, times(2)).evictFirstPages();

        int count = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Получение количества отложенных инвалидаций - успешно")
    void getPendingInvalidationsCount_whenQueueHasItems_shouldReturnCorrectCount() {
        assertEquals(0, cacheEventFallbackService.getPendingInvalidationsCount());

        cacheEventFallbackService.registerPendingInvalidation(TEST_CONVERSATION_ID, CacheEventType.MESSAGE_CREATED);
        assertEquals(1, cacheEventFallbackService.getPendingInvalidationsCount());

        cacheEventFallbackService.registerPendingInvalidationForMessage(TEST_MESSAGE_ID, CacheEventType.MESSAGE_UPDATED);
        assertEquals(2, cacheEventFallbackService.getPendingInvalidationsCount());

        cacheEventFallbackService.registerPendingInvalidationForUser(TEST_USER_ID, CacheEventType.MESSAGES_READ);
        assertEquals(3, cacheEventFallbackService.getPendingInvalidationsCount());
    }

    @Test
    @DisplayName("Очистка всех отложенных инвалидаций - успешно")
    void clearAllPendingInvalidations_whenCalled_shouldClearAllMaps() {
        cacheEventFallbackService.registerPendingInvalidation(TEST_CONVERSATION_ID, CacheEventType.MESSAGE_CREATED);
        cacheEventFallbackService.registerPendingInvalidationForMessage(TEST_MESSAGE_ID, CacheEventType.MESSAGE_UPDATED);
        cacheEventFallbackService.registerPendingInvalidationForUser(TEST_USER_ID, CacheEventType.MESSAGES_READ);

        int beforeCount = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(3, beforeCount);

        cacheEventFallbackService.clearAllPendingInvalidations();

        int afterCount = cacheEventFallbackService.getPendingInvalidationsCount();
        assertEquals(0, afterCount);
    }

    @Test
    @DisplayName("Выполнение инвалидации с повторными попытками для беседы - успех с первой попытки")
    void executeConversationInvalidationWithRetry_whenFirstAttemptSuccess_shouldNotRetry() {
        doNothing().when(messagingCacheService).evictConversationDetails(TEST_CONVERSATION_ID);
        doNothing().when(messagingCacheService).evictConversationMessages(TEST_CONVERSATION_ID);
        doNothing().when(messagingCacheService).evictFirstPages();

        cacheEventFallbackService.executeImmediateInvalidation(TEST_CONVERSATION_ID);

        verify(messagingCacheService, times(1)).evictConversationDetails(TEST_CONVERSATION_ID);
        verify(messagingCacheService, times(1)).evictConversationMessages(TEST_CONVERSATION_ID);
        verify(messagingCacheService, times(1)).evictFirstPages();
    }

    @Test
    @DisplayName("Выполнение инвалидации с повторными попытками для беседы - успех со второй попытки")
    void executeConversationInvalidationWithRetry_whenFirstAttemptFails_shouldRetry() {
        doThrow(new RuntimeException("First attempt failed"))
                .doNothing()
                .when(messagingCacheService).evictConversationDetails(TEST_CONVERSATION_ID);

        doNothing().when(messagingCacheService).evictConversationMessages(TEST_CONVERSATION_ID);
        doNothing().when(messagingCacheService).evictFirstPages();

        cacheEventFallbackService.executeImmediateInvalidation(TEST_CONVERSATION_ID);

        verify(messagingCacheService, times(2)).evictConversationDetails(TEST_CONVERSATION_ID);
        verify(messagingCacheService, times(1)).evictConversationMessages(TEST_CONVERSATION_ID);
        verify(messagingCacheService, times(1)).evictFirstPages();
    }

    @Test
    @DisplayName("Выполнение инвалидации с повторными попытками для беседы - все попытки провалены")
    void executeConversationInvalidationWithRetry_whenAllAttemptsFail_shouldLogError() {
        RuntimeException exception = new RuntimeException("Cache unavailable");

        doThrow(exception)
                .doThrow(exception)
                .doThrow(exception)
                .when(messagingCacheService).evictConversationDetails(TEST_CONVERSATION_ID);


        cacheEventFallbackService.executeImmediateInvalidation(TEST_CONVERSATION_ID);

        verify(messagingCacheService, times(3)).evictConversationDetails(TEST_CONVERSATION_ID);

        verify(messagingCacheService, never()).evictConversationMessages(TEST_CONVERSATION_ID);
        verify(messagingCacheService, never()).evictFirstPages();
    }

    @Test
    @DisplayName("Выполнение инвалидации с повторными попытками для сообщения - успех с первой попытки")
    void executeMessageInvalidationWithRetry_whenFirstAttemptSuccess_shouldNotRetry() {
        doNothing().when(messagingCacheService).evictMessage(TEST_MESSAGE_ID);

        cacheEventFallbackService.registerPendingInvalidationForMessage(TEST_MESSAGE_ID, CacheEventType.MESSAGE_UPDATED);
        cacheEventFallbackService.processPendingMessageInvalidations();

        verify(messagingCacheService, times(1)).evictMessage(TEST_MESSAGE_ID);
    }

    @Test
    @DisplayName("Выполнение инвалидации с повторными попытками для пользователя - успех с первой попытки")
    void executeUserInvalidationWithRetry_whenFirstAttemptSuccess_shouldNotRetry() {
        doNothing().when(messagingCacheService).evictUserConversations(TEST_USER_ID);
        doNothing().when(messagingCacheService).evictFirstPages();

        cacheEventFallbackService.registerPendingInvalidationForUser(TEST_USER_ID, CacheEventType.MESSAGES_READ);
        cacheEventFallbackService.processPendingUserInvalidations();

        verify(messagingCacheService, times(1)).evictUserConversations(TEST_USER_ID);
        verify(messagingCacheService, times(1)).evictFirstPages();
    }

    @Test
    @DisplayName("Интеграционный тест - обработка всех типов инвалидаций")
    void integrationTest_whenMultipleTypes_shouldProcessCorrectly() {
        UUID conversationId1 = UUID.randomUUID();
        UUID conversationId2 = UUID.randomUUID();
        UUID messageId1 = UUID.randomUUID();
        UUID messageId2 = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        cacheEventFallbackService.registerPendingInvalidation(conversationId1, CacheEventType.MESSAGE_CREATED);
        cacheEventFallbackService.registerPendingInvalidation(conversationId2, CacheEventType.MESSAGE_UPDATED);
        cacheEventFallbackService.registerPendingInvalidationForMessage(messageId1, CacheEventType.MESSAGE_DELETED);
        cacheEventFallbackService.registerPendingInvalidationForMessage(messageId2, CacheEventType.MESSAGE_IMAGE_UPLOADED);
        cacheEventFallbackService.registerPendingInvalidationForUser(userId1, CacheEventType.MESSAGES_READ);
        cacheEventFallbackService.registerPendingInvalidationForUser(userId2, CacheEventType.CONVERSATION_CREATED);

        assertEquals(6, cacheEventFallbackService.getPendingInvalidationsCount());

        doNothing().when(messagingCacheService).evictConversationDetails(any());
        doNothing().when(messagingCacheService).evictConversationMessages(any());
        doNothing().when(messagingCacheService).evictMessage(any());
        doNothing().when(messagingCacheService).evictUserConversations(any());
        doNothing().when(messagingCacheService).evictFirstPages();

        cacheEventFallbackService.processPendingConversationInvalidations();
        cacheEventFallbackService.processPendingMessageInvalidations();
        cacheEventFallbackService.processPendingUserInvalidations();

        verify(messagingCacheService, times(2)).evictConversationDetails(any());
        verify(messagingCacheService, times(2)).evictConversationMessages(any());
        verify(messagingCacheService, times(4)).evictFirstPages();
        verify(messagingCacheService, times(2)).evictMessage(any());
        verify(messagingCacheService, times(2)).evictUserConversations(any());

        assertEquals(0, cacheEventFallbackService.getPendingInvalidationsCount());
    }
}