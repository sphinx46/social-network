package ru.cs.vsu.social_network.messaging_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageEditRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationNotFoundException;
import ru.cs.vsu.social_network.messaging_service.exception.handler.GlobalExceptionHandler;
import ru.cs.vsu.social_network.messaging_service.exception.message.MessageNotFoundException;
import ru.cs.vsu.social_network.messaging_service.service.MessageService;
import ru.cs.vsu.social_network.messaging_service.service.MessagingService;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MessagingControllerTest extends BaseControllerTest {

    private static final UUID TEST_USER_ID = TestDataFactory.createTestUserId1();
    private static final UUID TEST_USER2_ID = TestDataFactory.createTestUserId2();
    private static final UUID TEST_CONVERSATION_ID = TestDataFactory.createTestConversationId();
    private static final UUID TEST_MESSAGE_ID = TestDataFactory.createTestMessageId();

    @Mock
    private MessagingService messagingService;

    @Mock
    private MessageService messageService;

    @Override
    protected Object controllerUnderTest() {
        return new MessagingController(messagingService, messageService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new GlobalExceptionHandler()};
    }

    @Test
    @DisplayName("Отправка сообщения - успешно")
    void sendMessage_whenRequestIsValid_shouldReturnOk() throws Exception {
        final MessageCreateRequest request = TestDataFactory.createMessageCreateRequest();
        final MessageResponse response = TestDataFactory.createMessageResponse();

        when(messagingService.sendMessage(eq(TEST_USER_ID), any(MessageCreateRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/send", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(TEST_MESSAGE_ID.toString()))
                .andExpect(jsonPath("$.senderId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.receiverId").value(TEST_USER2_ID.toString()));

        verify(messagingService).sendMessage(eq(TEST_USER_ID), any(MessageCreateRequest.class));
    }

    @Test
    @DisplayName("Отправка сообщения - отсутствует заголовок пользователя")
    void sendMessage_whenUserHeaderMissing_shouldReturnBadRequest() throws Exception {
        final MessageCreateRequest request = TestDataFactory.createMessageCreateRequest();

        mockMvcUtils.performPost("/send", request)
                .andExpect(status().isBadRequest());

        verify(messagingService, never()).sendMessage(any(), any());
    }

    @Test
    @DisplayName("Получение переписки между пользователями - успешно")
    void getConversationWithUser_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<ConversationDetailsResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createConversationDetailsResponse()));

        when(messagingService.getConversationWithUser(eq(TEST_USER_ID), eq(TEST_USER2_ID), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/conversation/" + TEST_USER2_ID +
                                "?size=10&pageNumber=0&sortedBy=createdAt&direction=DESC",
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].conversationId").value(TEST_CONVERSATION_ID.toString()));

        verify(messagingService).getConversationWithUser(eq(TEST_USER_ID), eq(TEST_USER2_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Получение бесед пользователя с предпросмотром - успешно")
    void getUserConversationsWithPreview_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<ConversationDetailsResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createConversationDetailsResponse()));

        when(messagingService.getUserConversationsWithPreview(eq(TEST_USER_ID), any(PageRequest.class), eq(3)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/conversations/preview?size=10&pageNumber=0&sortedBy=updatedAt&direction=DESC&previewLimit=3",
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(messagingService).getUserConversationsWithPreview(eq(TEST_USER_ID), any(PageRequest.class), eq(3));
    }

    @Test
    @DisplayName("Отметка беседы как прочитанной - успешно")
    void markConversationAsRead_whenRequestIsValid_shouldReturnOk() throws Exception {
        when(messagingService.markConversationAsRead(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID)))
                .thenReturn(5);

        mockMvcUtils.performPost("/conversation/" + TEST_CONVERSATION_ID + "/read",
                        null, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(messagingService).markConversationAsRead(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID));
    }

    @Test
    @DisplayName("Отметка беседы как прочитанной - беседа не найдена")
    void markConversationAsRead_whenConversationNotFound_shouldReturnNotFound() throws Exception {
        when(messagingService.markConversationAsRead(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID)))
                .thenThrow(new ConversationNotFoundException("Беседа не найдена"));

        mockMvcUtils.performPost("/conversation/" + TEST_CONVERSATION_ID + "/read",
                        null, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound());

        verify(messagingService).markConversationAsRead(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID));
    }

    @Test
    @DisplayName("Загрузка изображения для сообщения - успешно")
    void uploadMessageImage_whenRequestIsValid_shouldReturnOk() throws Exception {
        final MessageUploadImageRequest request = TestDataFactory.createMessageUploadImageRequest();
        final MessageResponse response = TestDataFactory.createMessageResponse();

        when(messagingService.uploadMessageImage(eq(TEST_USER_ID), any(MessageUploadImageRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/message/image/upload", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(TEST_MESSAGE_ID.toString()));

        verify(messagingService).uploadMessageImage(eq(TEST_USER_ID), any(MessageUploadImageRequest.class));
    }

    @Test
    @DisplayName("Удаление переписки с пользователем - успешно")
    void deleteConversationWithUser_whenRequestIsValid_shouldReturnNoContent() throws Exception {
        doNothing().when(messagingService).deleteConversationWithUser(eq(TEST_USER_ID), eq(TEST_USER2_ID));

        mockMvcUtils.performDelete("/conversation/delete/" + TEST_USER2_ID,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNoContent());

        verify(messagingService).deleteConversationWithUser(eq(TEST_USER_ID), eq(TEST_USER2_ID));
    }

    @Test
    @DisplayName("Получение информации о чате - успешно")
    void getChatInfo_whenRequestIsValid_shouldReturnOk() throws Exception {
        final ConversationDetailsResponse response = TestDataFactory.createConversationDetailsResponse();

        when(messagingService.getChatInfo(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID)))
                .thenReturn(response);

        mockMvcUtils.performGet("/chat/" + TEST_CONVERSATION_ID,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(TEST_CONVERSATION_ID.toString()));

        verify(messagingService).getChatInfo(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID));
    }

    @Test
    @DisplayName("Получение детальных бесед пользователя - успешно")
    void getUserConversationsDetailed_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<ConversationDetailsResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createConversationDetailsResponse()));

        when(messagingService.getUserConversationsDetailed(eq(TEST_USER_ID), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/conversations/detailed?size=10&pageNumber=0&sortedBy=updatedAt&direction=DESC",
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(messagingService).getUserConversationsDetailed(eq(TEST_USER_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Получение количества непрочитанных сообщений в беседе - успешно")
    void getUnreadMessagesCountInConversation_whenRequestIsValid_shouldReturnOk() throws Exception {
        when(messagingService.getUnreadMessagesCountInConversation(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID)))
                .thenReturn(5L);

        mockMvcUtils.performGet("/conversation/" + TEST_CONVERSATION_ID + "/unread",
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(messagingService).getUnreadMessagesCountInConversation(eq(TEST_USER_ID), eq(TEST_CONVERSATION_ID));
    }

    @Test
    @DisplayName("Редактирование сообщения - успешно")
    void editMessage_whenRequestIsValid_shouldReturnOk() throws Exception {
        final MessageEditRequest request = TestDataFactory.createMessageEditRequest();
        final MessageResponse response = TestDataFactory.createMessageResponse();

        when(messageService.editMessage(eq(TEST_USER_ID), any(MessageEditRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPut("/message/edit", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(TEST_MESSAGE_ID.toString()));

        verify(messageService).editMessage(eq(TEST_USER_ID), any(MessageEditRequest.class));
    }

    @Test
    @DisplayName("Редактирование сообщения - доступ запрещен")
    void editMessage_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {
        final MessageEditRequest request = TestDataFactory.createMessageEditRequest();

        when(messageService.editMessage(eq(TEST_USER2_ID), any(MessageEditRequest.class)))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvcUtils.performPut("/message/edit", request, "X-User-Id", TEST_USER2_ID.toString())
                .andExpect(status().isForbidden());

        verify(messageService).editMessage(eq(TEST_USER2_ID), any(MessageEditRequest.class));
    }

    @Test
    @DisplayName("Редактирование сообщения - сообщение не найдено")
    void editMessage_whenMessageNotFound_shouldReturnNotFound() throws Exception {
        final MessageEditRequest request = TestDataFactory.createMessageEditRequest();

        when(messageService.editMessage(eq(TEST_USER_ID), any(MessageEditRequest.class)))
                .thenThrow(new MessageNotFoundException("Сообщение не найдено"));

        mockMvcUtils.performPut("/message/edit", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound());

        verify(messageService).editMessage(eq(TEST_USER_ID), any(MessageEditRequest.class));
    }

    @Test
    @DisplayName("Получение сообщения по ID - успешно")
    void getMessageById_whenMessageExists_shouldReturnOk() throws Exception {
        final MessageResponse response = TestDataFactory.createMessageResponse();

        when(messageService.getMessageById(TEST_MESSAGE_ID)).thenReturn(response);

        mockMvcUtils.performGet("/" + TEST_MESSAGE_ID, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(TEST_MESSAGE_ID.toString()));

        verify(messageService).getMessageById(TEST_MESSAGE_ID);
    }

    @Test
    @DisplayName("Получение сообщения по ID - сообщение не найдено")
    void getMessageById_whenMessageNotFound_shouldReturnNotFound() throws Exception {
        when(messageService.getMessageById(TEST_MESSAGE_ID))
                .thenThrow(new MessageNotFoundException("Сообщение не найдено"));

        mockMvcUtils.performGet("/" + TEST_MESSAGE_ID, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound());

        verify(messageService).getMessageById(TEST_MESSAGE_ID);
    }

    @Test
    @DisplayName("Создание сообщения - пустой контент")
    void sendMessage_whenContentIsEmpty_shouldReturnBadRequest() throws Exception {
        final MessageCreateRequest request = TestDataFactory.createMessageCreateRequest(TEST_USER2_ID, "");

        mockMvcUtils.performPost("/send", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isBadRequest());

        verify(messagingService, never()).sendMessage(any(), any());
    }

    @Test
    @DisplayName("Создание сообщения - слишком длинный контент")
    void sendMessage_whenContentTooLong_shouldReturnBadRequest() throws Exception {
        final String longContent = "a".repeat(5001);
        final MessageCreateRequest request = TestDataFactory.createMessageCreateRequest(TEST_USER2_ID, longContent);

        mockMvcUtils.performPost("/send", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isBadRequest());

        verify(messagingService, never()).sendMessage(any(), any());
    }
}