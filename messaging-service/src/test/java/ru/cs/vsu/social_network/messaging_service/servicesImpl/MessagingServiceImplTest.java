package ru.cs.vsu.social_network.messaging_service.servicesImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationNotFoundException;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.service.ConversationService;
import ru.cs.vsu.social_network.messaging_service.service.MessageService;
import ru.cs.vsu.social_network.messaging_service.service.aggregator.ConversationDetailsAggregator;
import ru.cs.vsu.social_network.messaging_service.service.batch.BatchMessageService;
import ru.cs.vsu.social_network.messaging_service.service.serviceImpl.MessagingServiceImpl;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.messaging_service.validation.ConversationValidator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingServiceImplTest {

    @Mock
    private MessageService messageService;
    @Mock
    private ConversationService conversationService;
    @Mock
    private BatchMessageService batchMessageService;
    @Mock
    private ConversationDetailsAggregator conversationDetailsAggregator;
    @Mock
    private ConversationEntityProvider conversationEntityProvider;
    @Mock
    private EntityMapper entityMapper;
    @Mock
    private ConversationValidator conversationValidator;

    @InjectMocks
    private MessagingServiceImpl messagingService;

    @Test
    @DisplayName("Отправка сообщения - успешно")
    void sendMessage_whenValid_shouldReturnResponse() {
        UUID senderId = TestDataFactory.TEST_USER_ID;
        UUID receiverId = TestDataFactory.TEST_USER2_ID;
        MessageCreateRequest request = TestDataFactory.createMessageCreateRequest(receiverId, "Test message");
        ConversationResponse conversationResponse = TestDataFactory.createConversationResponse();
        MessageResponse messageResponse = TestDataFactory.createMessageResponse();

        when(conversationService.createOrGetConversation(senderId, receiverId)).thenReturn(conversationResponse);
        when(messageService.createMessage(senderId, request)).thenReturn(messageResponse);

        MessageResponse actual = messagingService.sendMessage(senderId, request);

        assertNotNull(actual);
        assertEquals(messageResponse, actual);
        verify(conversationService).createOrGetConversation(senderId, receiverId);
        verify(messageService).createMessage(senderId, request);
    }

    @Test
    @DisplayName("Получение переписки с пользователем - успешно")
    void getConversationWithUser_whenExists_shouldReturnPageResponse() {
        UUID user1Id = TestDataFactory.TEST_USER_ID;
        UUID user2Id = TestDataFactory.TEST_USER2_ID;
        PageRequest pageRequest = TestDataFactory.createPageRequest();
        ConversationDetailsResponse detailsResponse = TestDataFactory.createConversationDetailsResponse();
        PageResponse<ConversationDetailsResponse> expectedResponse = TestDataFactory.createPageResponse(List.of(detailsResponse));

        when(conversationService.getConversationBetweenUsersWithMessages(user1Id, user2Id, pageRequest))
                .thenReturn(expectedResponse);

        PageResponse<ConversationDetailsResponse> actual = messagingService
                .getConversationWithUser(user1Id, user2Id, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(conversationService).getConversationBetweenUsersWithMessages(user1Id, user2Id, pageRequest);
    }

    @Test
    @DisplayName("Получение бесед пользователя с предпросмотром - успешно")
    void getUserConversationsWithPreview_whenExists_shouldReturnPageResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        PageRequest pageRequest = TestDataFactory.createPageRequest();
        int previewLimit = 3;
        ConversationResponse conversationResponse = TestDataFactory.createConversationResponse();
        List<MessageResponse> previewMessages = List.of(TestDataFactory.createMessageResponse());
        PageResponse<ConversationResponse> conversationsPage = TestDataFactory.createPageResponse(List.of(conversationResponse));
        ConversationDetailsResponse detailsResponse = TestDataFactory.createConversationDetailsResponse();

        when(conversationService.getUserConversations(userId, pageRequest)).thenReturn(conversationsPage);
        when(batchMessageService.getMessagesByConversation(conversationResponse.getConversationId(), 0, previewLimit))
                .thenReturn(previewMessages);
        when(entityMapper.map(conversationResponse, ConversationDetailsResponse.class)).thenReturn(detailsResponse);

        PageResponse<ConversationDetailsResponse> actual = messagingService
                .getUserConversationsWithPreview(userId, pageRequest, previewLimit);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(conversationService).getUserConversations(userId, pageRequest);
        verify(batchMessageService).getMessagesByConversation(conversationResponse.getConversationId(), 0, previewLimit);
    }

    @Test
    @DisplayName("Отметка беседы как прочитанной - успешно")
    void markConversationAsRead_whenValid_shouldReturnCount() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        int expectedCount = 5;

        doNothing().when(conversationValidator).validateAccessToChat(conversationId, userId);
        when(messageService.markConversationAsRead(userId, conversationId)).thenReturn(expectedCount);

        int actual = messagingService.markConversationAsRead(userId, conversationId);

        assertEquals(expectedCount, actual);
        verify(conversationValidator).validateAccessToChat(conversationId, userId);
        verify(messageService).markConversationAsRead(userId, conversationId);
    }

    @Test
    @DisplayName("Загрузка изображения сообщения - успешно")
    void uploadMessageImage_whenValid_shouldReturnResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        MessageUploadImageRequest request = TestDataFactory.createMessageUploadImageRequest();
        MessageResponse expectedResponse = TestDataFactory.createMessageResponse();

        when(messageService.uploadImage(userId, request)).thenReturn(expectedResponse);

        MessageResponse actual = messagingService.uploadMessageImage(userId, request);

        assertEquals(expectedResponse, actual);
        verify(messageService).uploadImage(userId, request);
    }

    @Test
    @DisplayName("Удаление переписки с пользователем - успешно")
    void deleteConversationWithUser_whenExists_shouldDelete() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID otherUserId = TestDataFactory.TEST_USER2_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();

        when(conversationEntityProvider.getConversationBetweenUsers(userId, otherUserId))
                .thenReturn(Optional.of(conversation));
        doNothing().when(conversationService).deleteConversation(userId, conversation.getId());

        messagingService.deleteConversationWithUser(userId, otherUserId);

        verify(conversationEntityProvider).getConversationBetweenUsers(userId, otherUserId);
        verify(conversationService).deleteConversation(userId, conversation.getId());
    }

    @Test
    @DisplayName("Удаление переписки с пользователем - не найдена")
    void deleteConversationWithUser_whenNotExists_shouldThrowException() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID otherUserId = TestDataFactory.TEST_USER2_ID;

        when(conversationEntityProvider.getConversationBetweenUsers(userId, otherUserId))
                .thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> messagingService.deleteConversationWithUser(userId, otherUserId));

        verify(conversationEntityProvider).getConversationBetweenUsers(userId, otherUserId);
        verify(conversationService, never()).deleteConversation(any(), any());
    }

    @Test
    @DisplayName("Получение информации о чате - успешно")
    void getChatInfo_whenValid_shouldReturnResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        ConversationDetailsResponse expectedResponse = TestDataFactory.createConversationDetailsResponse();

        doNothing().when(conversationValidator).validateAccessToChat(conversationId, userId);
        when(conversationService.getConversationWithMessages(conversationId, 3)).thenReturn(expectedResponse);

        ConversationDetailsResponse actual = messagingService.getChatInfo(userId, conversationId);

        assertEquals(expectedResponse, actual);
        verify(conversationValidator).validateAccessToChat(conversationId, userId);
        verify(conversationService).getConversationWithMessages(conversationId, 3);
    }

    @Test
    @DisplayName("Получение детальных бесед пользователя - успешно")
    void getUserConversationsDetailed_whenExists_shouldReturnPageResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        PageRequest pageRequest = TestDataFactory.createPageRequest();
        List<Conversation> conversations = TestDataFactory.createConversationList(2);
        Page<Conversation> conversationsPage = new PageImpl<>(conversations, pageRequest.toPageable(), 2);
        ConversationDetailsResponse detailsResponse = TestDataFactory.createConversationDetailsResponse();
        Page<ConversationDetailsResponse> aggregatedPage = new PageImpl<>(
                List.of(detailsResponse), pageRequest.toPageable(), 2
        );

        when(conversationEntityProvider.getConversationsByUser(userId, pageRequest.getPageNumber(), pageRequest.getSize()))
                .thenReturn(conversations);
        when(conversationEntityProvider.getConversationsCountByUser(userId)).thenReturn(2L);
        when(conversationDetailsAggregator.aggregateConversationsPage(conversationsPage, true, 3))
                .thenReturn(aggregatedPage);

        PageResponse<ConversationDetailsResponse> actual = messagingService
                .getUserConversationsDetailed(userId, pageRequest);

        assertNotNull(actual);
        verify(conversationEntityProvider).getConversationsByUser(userId, pageRequest.getPageNumber(), pageRequest.getSize());
        verify(conversationDetailsAggregator).aggregateConversationsPage(conversationsPage, true, 3);
    }

    @Test
    @DisplayName("Получение количества непрочитанных в беседе - успешно")
    void getUnreadMessagesCountInConversation_whenExists_shouldReturnCount() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Long expectedCount = 5L;

        doNothing().when(conversationValidator).validateAccessToChat(conversationId, userId);
        when(batchMessageService.getAllUnreadMessagesCountInConversation(userId, conversationId))
                .thenReturn(expectedCount);

        Long actual = messagingService.getUnreadMessagesCountInConversation(userId, conversationId);

        assertEquals(expectedCount, actual);
        verify(conversationValidator).validateAccessToChat(conversationId, userId);
        verify(batchMessageService).getAllUnreadMessagesCountInConversation(userId, conversationId);
    }
}