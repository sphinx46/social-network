package ru.cs.vsu.social_network.messaging_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.websocket.TypingIndicator;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationNotFoundException;
import ru.cs.vsu.social_network.messaging_service.exception.handler.GlobalExceptionHandler;
import ru.cs.vsu.social_network.messaging_service.service.WebSocketMessagingService;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest extends BaseControllerTest {

    private static final UUID TEST_USER_ID = TestDataFactory.createTestUserId1();
    private static final UUID TEST_USER2_ID = TestDataFactory.createTestUserId2();
    private static final UUID TEST_CONVERSATION_ID = TestDataFactory.createTestConversationId();
    private static final UUID TEST_MESSAGE_ID = TestDataFactory.createTestMessageId();

    @Mock
    private WebSocketMessagingService webSocketMessagingService;

    @Override
    protected Object controllerUnderTest() {
        return new WebSocketMessagingController(webSocketMessagingService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new GlobalExceptionHandler()};
    }

    @Test
    @DisplayName("Отправка сообщения с WebSocket уведомлением - успешно")
    void sendMessageWithNotification_whenRequestIsValid_shouldReturnOk() throws Exception {
        final MessageCreateRequest request = TestDataFactory.createMessageCreateRequest();
        final MessageResponse response = TestDataFactory.createMessageResponse();

        when(webSocketMessagingService.sendMessageWithNotification(eq(TEST_USER_ID), any(MessageCreateRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/websocket-messaging/send-with-notification", request,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(TEST_MESSAGE_ID.toString()));

        verify(webSocketMessagingService).sendMessageWithNotification(eq(TEST_USER_ID), any(MessageCreateRequest.class));
    }

    @Test
    @DisplayName("Отправка сообщения с WebSocket уведомлением - отсутствует заголовок пользователя")
    void sendMessageWithNotification_whenUserHeaderMissing_shouldReturnBadRequest() throws Exception {
        final MessageCreateRequest request = TestDataFactory.createMessageCreateRequest();

        mockMvcUtils.performPost("/websocket-messaging/send-with-notification", request)
                .andExpect(status().isBadRequest());

        verify(webSocketMessagingService, never()).sendMessageWithNotification(any(), any());
    }

    @Test
    @DisplayName("Отметка беседы как прочитанной с WebSocket уведомлением - успешно")
    void markConversationAsReadWithNotification_whenRequestIsValid_shouldReturnOk() throws Exception {
        when(webSocketMessagingService.markConversationAsReadWithNotification(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID)))
                .thenReturn(3);

        mockMvcUtils.performPost("/websocket-messaging/conversation/" + TEST_CONVERSATION_ID + "/read-with-notification",
                        null, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        verify(webSocketMessagingService).markConversationAsReadWithNotification(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID));
    }

    @Test
    @DisplayName("Отметка беседы как прочитанной с WebSocket уведомлением - беседа не найдена")
    void markConversationAsReadWithNotification_whenConversationNotFound_shouldReturnNotFound() throws Exception {
        when(webSocketMessagingService.markConversationAsReadWithNotification(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID)))
                .thenThrow(new ConversationNotFoundException("Беседа не найдена"));

        mockMvcUtils.performPost("/websocket-messaging/conversation/" + TEST_CONVERSATION_ID + "/read-with-notification",
                        null, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound());

        verify(webSocketMessagingService).markConversationAsReadWithNotification(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID));
    }

    @Test
    @DisplayName("Загрузка изображения для сообщения с WebSocket уведомлением - успешно")
    void uploadMessageImageWithNotification_whenRequestIsValid_shouldReturnOk() throws Exception {
        final MessageUploadImageRequest request = TestDataFactory.createMessageUploadImageRequest();
        final MessageResponse response = TestDataFactory.createMessageResponse();

        when(webSocketMessagingService.uploadMessageImageWithNotification(eq(TEST_USER_ID), any(MessageUploadImageRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/websocket-messaging/message/image/upload-with-notification", request,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(TEST_MESSAGE_ID.toString()));

        verify(webSocketMessagingService).uploadMessageImageWithNotification(eq(TEST_USER_ID), any(MessageUploadImageRequest.class));
    }

    @Test
    @DisplayName("Отправка индикатора печатания - успешно")
    void sendTypingNotification_whenRequestIsValid_shouldReturnOk() throws Exception {
        final TypingIndicator typingIndicator = TestDataFactory.createTypingIndicator();

        doNothing().when(webSocketMessagingService).sendTypingNotification(
                eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), eq(true));

        mockMvcUtils.performPost("/websocket-messaging/typing", typingIndicator,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk());

        verify(webSocketMessagingService).sendTypingNotification(
                eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), eq(true));
    }

    @Test
    @DisplayName("Отправка индикатора печатания - отсутствует заголовок пользователя")
    void sendTypingNotification_whenUserHeaderMissing_shouldReturnBadRequest() throws Exception {
        final TypingIndicator typingIndicator = TestDataFactory.createTypingIndicator();

        mockMvcUtils.performPost("/websocket-messaging/typing", typingIndicator)
                .andExpect(status().isBadRequest());

        verify(webSocketMessagingService, never()).sendTypingNotification(any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Отправка сообщения с WebSocket уведомлением - пустой контент")
    void sendMessageWithNotification_whenContentIsEmpty_shouldReturnBadRequest() throws Exception {
        final MessageCreateRequest request = TestDataFactory.createMessageCreateRequest(TEST_USER2_ID, "");

        mockMvcUtils.performPost("/websocket-messaging/send-with-notification", request,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isBadRequest());

        verify(webSocketMessagingService, never()).sendMessageWithNotification(any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения - любой непустой URL проходит валидацию")
    void uploadMessageImageWithNotification_whenAnyNonEmptyUrl_shouldReturnOk() throws Exception {
        final MessageUploadImageRequest request = TestDataFactory.createMessageUploadImageRequest(TEST_MESSAGE_ID, "invalid-url");
        final MessageResponse response = TestDataFactory.createMessageResponse();

        when(webSocketMessagingService.uploadMessageImageWithNotification(eq(TEST_USER_ID), any(MessageUploadImageRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/websocket-messaging/message/image/upload-with-notification", request,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk());

        verify(webSocketMessagingService).uploadMessageImageWithNotification(eq(TEST_USER_ID), any(MessageUploadImageRequest.class));
    }
}