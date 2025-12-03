package ru.cs.vsu.social_network.messaging_service.servicesImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.service.MessagingService;
import ru.cs.vsu.social_network.messaging_service.service.serviceImpl.WebSocketMessagingServiceImpl;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketMessagingServiceImplTest {

    @Mock
    private MessagingService messagingService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ConversationEntityProvider conversationEntityProvider;

    @InjectMocks
    private WebSocketMessagingServiceImpl webSocketMessagingService;

    @Test
    @DisplayName("Отправка сообщения с уведомлением - успешно")
    void sendMessageWithNotification_whenValid_shouldReturnResponseAndSendNotifications() {
        UUID senderId = TestDataFactory.TEST_USER_ID;
        UUID receiverId = TestDataFactory.TEST_USER2_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        MessageCreateRequest request = TestDataFactory.createMessageCreateRequest(receiverId, "Test message");
        MessageResponse messageResponse = TestDataFactory.createMessageResponse();
        messageResponse.setConversationId(conversationId);

        when(messagingService.sendMessage(senderId, request)).thenReturn(messageResponse);

        MessageResponse actual = webSocketMessagingService.sendMessageWithNotification(senderId, request);

        assertNotNull(actual);
        assertEquals(messageResponse, actual);
        verify(messagingService).sendMessage(senderId, request);
        verify(messagingTemplate).convertAndSendToUser(
                eq(receiverId.toString()),
                eq("/queue/messages"),
                eq(messageResponse)
        );
        verify(messagingTemplate).convertAndSend(
                eq("/topic/conversation/" + conversationId),
                eq(messageResponse)
        );
    }

    @Test
    @DisplayName("Отправка сообщения с уведомлением - WebSocket ошибка")
    void sendMessageWithNotification_whenWebSocketFails_shouldStillReturnResponse() {
        UUID senderId = TestDataFactory.TEST_USER_ID;
        UUID receiverId = TestDataFactory.TEST_USER2_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        MessageCreateRequest request = TestDataFactory.createMessageCreateRequest(receiverId, "Test message");
        MessageResponse messageResponse = TestDataFactory.createMessageResponse();
        messageResponse.setConversationId(conversationId);

        when(messagingService.sendMessage(senderId, request)).thenReturn(messageResponse);
        doThrow(new RuntimeException("WebSocket error")).when(messagingTemplate)
                .convertAndSendToUser(anyString(), anyString(), any(MessageResponse.class));

        MessageResponse actual = webSocketMessagingService.sendMessageWithNotification(senderId, request);

        assertNotNull(actual);
        assertEquals(messageResponse, actual);
        verify(messagingService).sendMessage(senderId, request);
    }

    @Test
    @DisplayName("Отметка беседы как прочитанной с уведомлением - успешно")
    void markConversationAsReadWithNotification_whenValid_shouldReturnCountAndSendNotification() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID interlocutorId = TestDataFactory.TEST_USER2_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        int markedCount = 5;

        when(messagingService.markConversationAsRead(userId, conversationId)).thenReturn(markedCount);
        when(conversationEntityProvider.getInterlocutorId(conversationId, userId))
                .thenReturn(Optional.of(interlocutorId));

        int actual = webSocketMessagingService.markConversationAsReadWithNotification(userId, conversationId);

        assertEquals(markedCount, actual);
        verify(messagingService).markConversationAsRead(userId, conversationId);
        verify(messagingTemplate).convertAndSendToUser(
                eq(interlocutorId.toString()),
                eq("/queue/message-status"),
                any()
        );
    }

    @Test
    @DisplayName("Загрузка изображения сообщения с уведомлением - успешно")
    void uploadMessageImageWithNotification_whenValid_shouldReturnResponseAndSendNotification() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        MessageUploadImageRequest request = TestDataFactory.createMessageUploadImageRequest();
        MessageResponse messageResponse = TestDataFactory.createMessageResponse();
        messageResponse.setConversationId(conversationId);

        when(messagingService.uploadMessageImage(userId, request)).thenReturn(messageResponse);

        MessageResponse actual = webSocketMessagingService.uploadMessageImageWithNotification(userId, request);

        assertNotNull(actual);
        assertEquals(messageResponse, actual);
        verify(messagingService).uploadMessageImage(userId, request);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/conversation/" + conversationId),
                eq(messageResponse)
        );
    }

    @Test
    @DisplayName("Отправка индикатора печатания - успешно")
    void sendTypingNotification_whenValid_shouldSendNotification() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        boolean isTyping = true;

        webSocketMessagingService.sendTypingNotification(conversationId, userId, isTyping);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/typing/" + conversationId),
                any(ru.cs.vsu.social_network.messaging_service.dto.request.websocket.TypingIndicator.class)
        );
    }

    @Test
    @DisplayName("Отправка индикатора печатания - WebSocket ошибка")
    void sendTypingNotification_whenWebSocketFails_shouldNotThrow() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        boolean isTyping = true;

        doThrow(new RuntimeException("WebSocket error")).when(messagingTemplate)
                .convertAndSend(anyString(), any(ru.cs.vsu.social_network.messaging_service.dto.request.websocket.TypingIndicator.class));

        assertDoesNotThrow(() ->
                webSocketMessagingService.sendTypingNotification(conversationId, userId, isTyping)
        );

        verify(messagingTemplate).convertAndSend(
                eq("/topic/typing/" + conversationId),
                any(ru.cs.vsu.social_network.messaging_service.dto.request.websocket.TypingIndicator.class)
        );
    }
}