package ru.cs.vsu.social_network.messaging_service.servicesImpl.batchImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.provider.MessageEntityProvider;
import ru.cs.vsu.social_network.messaging_service.repository.MessageRepository;
import ru.cs.vsu.social_network.messaging_service.service.batch.batchImpl.BatchMessageServiceImpl;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchMessageServiceImplTest {

    @Mock
    private MessageEntityProvider messageEntityProvider;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private BatchMessageServiceImpl batchMessageService;

    @Test
    @DisplayName("Получение количества непрочитанных сообщений пользователя - успешно")
    void getUnreadMessagesCountByUser_whenValid_shouldReturnCount() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        MessageStatus status = MessageStatus.SENT;
        Long expectedCount = 10L;

        when(messageEntityProvider.getUnreadMessagesCountByUser(receiverId, status))
                .thenReturn(expectedCount);

        Long actual = batchMessageService.getUnreadMessagesCountByUser(receiverId, status);

        assertEquals(expectedCount, actual);
        verify(messageEntityProvider).getUnreadMessagesCountByUser(receiverId, status);
    }

    @Test
    @DisplayName("Получение количества непрочитанных сообщений пользователя - исключение")
    void getUnreadMessagesCountByUser_whenException_shouldReturnZero() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        MessageStatus status = MessageStatus.SENT;

        when(messageEntityProvider.getUnreadMessagesCountByUser(receiverId, status))
                .thenThrow(new RuntimeException("Database error"));

        Long actual = batchMessageService.getUnreadMessagesCountByUser(receiverId, status);

        assertEquals(0L, actual);
        verify(messageEntityProvider).getUnreadMessagesCountByUser(receiverId, status);
    }

    @Test
    @DisplayName("Получение количества непрочитанных сообщений в беседах - успешно")
    void getUnreadMessagesCountInConversations_whenValid_shouldReturnMap() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        List<UUID> conversationIds = TestDataFactory.createMessageIds(3);
        MessageStatus status = MessageStatus.SENT;
        Map<UUID, Long> expectedMap = TestDataFactory.createUnreadMessagesCountMap(conversationIds);

        when(messageEntityProvider.getUnreadMessagesCountInConversation(any(UUID.class), any(UUID.class), eq(status)))
                .thenAnswer(invocation -> 5L);

        Map<UUID, Long> actual = batchMessageService.getUnreadMessagesCountInConversations(receiverId, conversationIds, status);

        assertNotNull(actual);
        assertEquals(conversationIds.size(), actual.size());
        conversationIds.forEach(conversationId -> assertTrue(actual.containsKey(conversationId)));
        verify(messageEntityProvider, times(conversationIds.size())).getUnreadMessagesCountInConversation(any(), any(), any());
    }

    @Test
    @DisplayName("Получение количества непрочитанных сообщений в беседах - пустой список")
    void getUnreadMessagesCountInConversations_whenEmptyList_shouldReturnEmptyMap() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        List<UUID> emptyList = List.of();
        MessageStatus status = MessageStatus.SENT;

        Map<UUID, Long> actual = batchMessageService.getUnreadMessagesCountInConversations(receiverId, emptyList, status);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(messageEntityProvider, never()).getUnreadMessagesCountInConversation(any(), any(), any());
    }

    @Test
    @DisplayName("Получение количества непрочитанных сообщений в беседах - превышение максимального размера")
    void getUnreadMessagesCountInConversations_whenExceedsMaxSize_shouldUseBatch() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        List<UUID> conversationIds = TestDataFactory.createMessageIds(1500);
        MessageStatus status = MessageStatus.SENT;

        when(messageEntityProvider.getUnreadMessagesCountInConversation(any(UUID.class), any(UUID.class), eq(status)))
                .thenReturn(5L);

        Map<UUID, Long> actual = batchMessageService.getUnreadMessagesCountInConversations(receiverId, conversationIds, status);

        assertNotNull(actual);
        assertEquals(1000, actual.size());
        verify(messageEntityProvider, atLeast(1000)).getUnreadMessagesCountInConversation(any(), any(), any());
    }

    @Test
    @DisplayName("Получение последних сообщений для бесед - успешно")
    void getRecentMessagesForConversations_whenValid_shouldReturnMap() {
        List<UUID> conversationIds = TestDataFactory.createMessageIds(3);
        int messagesLimit = 5;
        List<Message> mockMessages = TestDataFactory.createMessageEntityList(2, conversationIds.get(0));
        Map<UUID, List<MessageResponse>> expectedMap = TestDataFactory.createRecentMessagesMap(conversationIds, messagesLimit);

        when(messageEntityProvider.getRecentMessagesForConversations(anyList(), eq(messagesLimit)))
                .thenReturn(mockMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        Map<UUID, List<MessageResponse>> actual = batchMessageService.getRecentMessagesForConversations(conversationIds, messagesLimit);

        assertNotNull(actual);
        assertEquals(conversationIds.size(), actual.size());
        conversationIds.forEach(conversationId -> assertTrue(actual.containsKey(conversationId)));
        verify(messageEntityProvider).getRecentMessagesForConversations(anyList(), eq(messagesLimit));
    }

    @Test
    @DisplayName("Получение последних сообщений для бесед - лимит меньше 1")
    void getRecentMessagesForConversations_whenLimitLessThanOne_shouldUseDefaultLimit() {
        List<UUID> conversationIds = TestDataFactory.createMessageIds(2);
        int invalidLimit = 0;
        int effectiveLimit = 1;
        List<Message> mockMessages = TestDataFactory.createMessageEntityList(1, conversationIds.get(0));

        when(messageEntityProvider.getRecentMessagesForConversations(anyList(), eq(effectiveLimit)))
                .thenReturn(mockMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        Map<UUID, List<MessageResponse>> actual = batchMessageService.getRecentMessagesForConversations(conversationIds, invalidLimit);

        assertNotNull(actual);
        assertEquals(conversationIds.size(), actual.size());
        verify(messageEntityProvider).getRecentMessagesForConversations(anyList(), eq(effectiveLimit));
    }

    @Test
    @DisplayName("Получение сообщений беседы - успешно")
    void getMessagesByConversation_whenValid_shouldReturnMessages() {
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        int page = 0;
        int size = 10;
        List<Message> mockMessages = TestDataFactory.createMessageEntityList(3, conversationId);

        when(messageEntityProvider.getMessagesByConversation(conversationId, page, size))
                .thenReturn(mockMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        List<MessageResponse> actual = batchMessageService.getMessagesByConversation(conversationId, page, size);

        assertNotNull(actual);
        assertEquals(mockMessages.size(), actual.size());
        verify(messageEntityProvider).getMessagesByConversation(conversationId, page, size);
    }

    @Test
    @DisplayName("Получение сообщений между пользователями - успешно")
    void getMessagesBetweenUsers_whenValid_shouldReturnMessages() {
        UUID senderId = TestDataFactory.TEST_USER_ID;
        UUID receiverId = TestDataFactory.TEST_USER2_ID;
        int page = 0;
        int size = 10;
        List<Message> mockMessages = TestDataFactory.createMessageEntityList(3, TestDataFactory.TEST_CONVERSATION_ID);

        when(messageEntityProvider.getMessagesBetweenUsers(senderId, receiverId, page, size))
                .thenReturn(mockMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        List<MessageResponse> actual = batchMessageService.getMessagesBetweenUsers(senderId, receiverId, page, size);

        assertNotNull(actual);
        assertEquals(mockMessages.size(), actual.size());
        verify(messageEntityProvider).getMessagesBetweenUsers(senderId, receiverId, page, size);
    }

    @Test
    @DisplayName("Получение сообщений с беседами - успешно")
    void getMessagesWithConversations_whenValid_shouldReturnMessages() {
        List<UUID> messageIds = TestDataFactory.createMessageIds(5);
        List<Message> mockMessages = TestDataFactory.createMessageEntityList(3, TestDataFactory.TEST_CONVERSATION_ID);

        when(messageEntityProvider.getMessagesWithConversations(anyList()))
                .thenReturn(mockMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        List<MessageResponse> actual = batchMessageService.getMessagesWithConversations(messageIds);

        assertNotNull(actual);
        verify(messageEntityProvider).getMessagesWithConversations(anyList());
    }

    @Test
    @DisplayName("Получение непрочитанных сообщений в беседе - успешно")
    void getUnreadMessagesInConversation_whenValid_shouldReturnMessages() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        MessageStatus status = MessageStatus.SENT;
        List<Message> mockMessages = TestDataFactory.createUnreadMessageEntityList(3, receiverId, conversationId);

        when(messageEntityProvider.getUnreadMessagesInConversation(receiverId, conversationId, status))
                .thenReturn(mockMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        List<MessageResponse> actual = batchMessageService.getUnreadMessagesInConversation(receiverId, conversationId, status);

        assertNotNull(actual);
        assertEquals(mockMessages.size(), actual.size());
        verify(messageEntityProvider).getUnreadMessagesInConversation(receiverId, conversationId, status);
    }

    @Test
    @DisplayName("Получение всех непрочитанных сообщений в беседе - успешно")
    void getAllUnreadMessagesInConversation_whenValid_shouldReturnMessages() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        List<Message> sentMessages = TestDataFactory.createUnreadMessageEntityList(2, receiverId, conversationId);
        sentMessages.forEach(m -> m.setStatus(MessageStatus.SENT));
        List<Message> deliveredMessages = TestDataFactory.createUnreadMessageEntityList(3, receiverId, conversationId);
        deliveredMessages.forEach(m -> m.setStatus(MessageStatus.DELIVERED));

        when(messageEntityProvider.getUnreadMessagesInConversation(receiverId, conversationId, MessageStatus.SENT))
                .thenReturn(sentMessages);
        when(messageEntityProvider.getUnreadMessagesInConversation(receiverId, conversationId, MessageStatus.DELIVERED))
                .thenReturn(deliveredMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        List<MessageResponse> actual = batchMessageService.getAllUnreadMessagesInConversation(receiverId, conversationId);

        assertNotNull(actual);
        assertEquals(sentMessages.size() + deliveredMessages.size(), actual.size());
        verify(messageEntityProvider).getUnreadMessagesInConversation(receiverId, conversationId, MessageStatus.SENT);
        verify(messageEntityProvider).getUnreadMessagesInConversation(receiverId, conversationId, MessageStatus.DELIVERED);
    }

    @Test
    @DisplayName("Получение общего количества непрочитанных сообщений в беседе - успешно")
    void getAllUnreadMessagesCountInConversation_whenValid_shouldReturnCount() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Long sentCount = 3L;
        Long deliveredCount = 2L;
        Long expectedTotal = 5L;

        when(messageEntityProvider.getUnreadMessagesCountInConversation(receiverId, conversationId, MessageStatus.SENT))
                .thenReturn(sentCount);
        when(messageEntityProvider.getUnreadMessagesCountInConversation(receiverId, conversationId, MessageStatus.DELIVERED))
                .thenReturn(deliveredCount);

        Long actual = batchMessageService.getAllUnreadMessagesCountInConversation(receiverId, conversationId);

        assertEquals(expectedTotal, actual);
        verify(messageEntityProvider).getUnreadMessagesCountInConversation(receiverId, conversationId, MessageStatus.SENT);
        verify(messageEntityProvider).getUnreadMessagesCountInConversation(receiverId, conversationId, MessageStatus.DELIVERED);
    }

    @Test
    @DisplayName("Получение общего количества непрочитанных сообщений во многих беседах - успешно")
    void getAllUnreadMessagesCountInConversations_whenValid_shouldReturnMap() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        List<UUID> conversationIds = TestDataFactory.createMessageIds(3);
        Map<UUID, Long> expectedMap = TestDataFactory.createUnreadMessagesCountMap(conversationIds);

        when(messageEntityProvider.getUnreadMessagesCountInConversation(any(UUID.class), any(UUID.class), eq(MessageStatus.SENT)))
                .thenReturn(3L);
        when(messageEntityProvider.getUnreadMessagesCountInConversation(any(UUID.class), any(UUID.class), eq(MessageStatus.DELIVERED)))
                .thenReturn(2L);

        Map<UUID, Long> actual = batchMessageService.getAllUnreadMessagesCountInConversations(receiverId, conversationIds);

        assertNotNull(actual);
        assertEquals(conversationIds.size(), actual.size());
        conversationIds.forEach(conversationId -> {
            assertTrue(actual.containsKey(conversationId));
            assertEquals(5L, actual.get(conversationId));
        });
    }

    @Test
    @DisplayName("Получение валидных сообщений для обновления статуса - успешно")
    void getValidMessagesForStatusUpdate_whenValid_shouldReturnMessages() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        List<UUID> messageIds = TestDataFactory.createMessageIds(5);
        MessageStatus requiredStatus = MessageStatus.SENT;
        List<Message> mockMessages = TestDataFactory.createMessageEntityList(3, TestDataFactory.TEST_CONVERSATION_ID);
        mockMessages.forEach(m -> {
            m.setReceiverId(receiverId);
            m.setStatus(requiredStatus);
        });

        when(messageEntityProvider.getMessagesWithConversations(anyList()))
                .thenReturn(mockMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        List<MessageResponse> actual = batchMessageService.getValidMessagesForStatusUpdate(receiverId, messageIds, requiredStatus);

        assertNotNull(actual);
        assertEquals(mockMessages.size(), actual.size());
        verify(messageEntityProvider).getMessagesWithConversations(anyList());
    }

    @Test
    @DisplayName("Получение сообщений между пользователями по статусам - успешно")
    void getMessagesBetweenUsersWithStatuses_whenValid_shouldReturnMessages() {
        UUID senderId = TestDataFactory.TEST_USER_ID;
        UUID receiverId = TestDataFactory.TEST_USER2_ID;
        List<MessageStatus> statuses = List.of(MessageStatus.SENT, MessageStatus.DELIVERED);
        int page = 0;
        int size = 10;
        List<Message> mockMessages = TestDataFactory.createMessageEntityList(5, TestDataFactory.TEST_CONVERSATION_ID);
        mockMessages.forEach(m -> m.setStatus(statuses.get(mockMessages.indexOf(m) % statuses.size())));

        when(messageEntityProvider.getMessagesBetweenUsers(senderId, receiverId, page, size * 2))
                .thenReturn(mockMessages);
        when(entityMapper.map(any(Message.class), eq(MessageResponse.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            return TestDataFactory.createMessageResponse(
                    message.getId(),
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getConversation().getId()
            );
        });

        List<MessageResponse> actual = batchMessageService.getMessagesBetweenUsersWithStatuses(senderId, receiverId, statuses, page, size);

        assertNotNull(actual);
        verify(messageEntityProvider).getMessagesBetweenUsers(senderId, receiverId, page, size * 2);
    }

    @Test
    @DisplayName("Пакетная отметка беседы как прочитанной - успешно")
    void batchMarkConversationAsRead_whenValid_shouldReturnCount() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        int expectedCount = 5;

        when(messageRepository.updateMessagesStatusesByConversation(
                eq(receiverId),
                eq(conversationId),
                anyList(),
                eq(MessageStatus.READ)
        )).thenReturn(expectedCount);

        int actual = batchMessageService.batchMarkConversationAsRead(receiverId, conversationId);

        assertEquals(expectedCount, actual);
        verify(messageRepository).updateMessagesStatusesByConversation(
                eq(receiverId),
                eq(conversationId),
                anyList(),
                eq(MessageStatus.READ)
        );
    }

    @Test
    @DisplayName("Пакетное обновление статусов сообщений - успешно")
    void batchUpdateMessagesStatus_whenValid_shouldReturnCount() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        List<UUID> messageIds = TestDataFactory.createMessageIds(5);
        MessageStatus oldStatus = MessageStatus.SENT;
        MessageStatus newStatus = MessageStatus.DELIVERED;
        int expectedCount = 3;

        when(messageRepository.updateMessagesStatusWithReceiverCheck(
                anyList(),
                eq(receiverId),
                eq(oldStatus),
                eq(newStatus)
        )).thenReturn(expectedCount);

        int actual = batchMessageService.batchUpdateMessagesStatus(receiverId, messageIds, oldStatus, newStatus);

        assertEquals(expectedCount, actual);
        verify(messageRepository, atLeastOnce()).updateMessagesStatusWithReceiverCheck(
                anyList(),
                eq(receiverId),
                eq(oldStatus),
                eq(newStatus)
        );
    }

    @Test
    @DisplayName("Пакетное обновление статусов сообщений - пустой список")
    void batchUpdateMessagesStatus_whenEmptyList_shouldReturnZero() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        List<UUID> emptyList = List.of();
        MessageStatus oldStatus = MessageStatus.SENT;
        MessageStatus newStatus = MessageStatus.DELIVERED;

        int actual = batchMessageService.batchUpdateMessagesStatus(receiverId, emptyList, oldStatus, newStatus);

        assertEquals(0, actual);
        verify(messageRepository, never()).updateMessagesStatusWithReceiverCheck(anyList(), any(), any(), any());
    }
}