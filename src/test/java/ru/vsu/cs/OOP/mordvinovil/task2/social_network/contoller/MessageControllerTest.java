package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.MessageController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.MessageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest extends BaseControllerTest {

    @MockitoBean
    private MessageService messageService;

    @Test
    @DisplayName("Создание сообщения без авторизации - должно вернуть 401")
    void createMessage_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createMessageRequest();

        mockMvcUtils.performPost("/messages/create", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Создание сообщения - успешное создание")
    void createMessage_whenValidData_shouldCreateMessage() throws Exception {
        var request = TestDataFactory.createMessageRequest();
        var response = TestDataFactory.createMessageResponse();

        when(messageService.create(any(), any())).thenReturn(response);

        mockMvcUtils.performPost("/messages/create", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.senderUsername").value("testUser"))
                .andExpect(jsonPath("$.receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.content").value("Тестовое сообщение"))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(messageService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Создание сообщения - когда сервис выбрасывает исключение")
    void createMessage_whenServiceThrowsException_shouldReturnError() throws Exception {
        var request = TestDataFactory.createMessageRequest();

        when(messageService.create(any(), any()))
                .thenThrow(new RuntimeException("Ошибка создания сообщения"));

        mockMvcUtils.performPost("/messages/create", request)
                .andExpect(status().isInternalServerError());

        verify(messageService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение сообщения по ID - успешно")
    void getMessage_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var response = TestDataFactory.createMessageResponse();

        when(messageService.getMessageById(eq(messageId), any())).thenReturn(response);

        mockMvcUtils.performGet("/messages/" + messageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.senderUsername").value("testUser"))
                .andExpect(jsonPath("$.receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.content").value("Тестовое сообщение"));

        verify(messageService, times(1)).getMessageById(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение сообщения по ID без авторизации - должно вернуть 401")
    void getMessage_whenUnAuthorized_shouldReturn401() throws Exception {
        Long messageId = 1L;

        mockMvcUtils.performGet("/messages/" + messageId)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение переписки с пользователем - успешно")
    void getConversation_whenRequestIsValid() throws Exception {
        Long userId = 2L;
        var responses = List.of(TestDataFactory.createMessageResponse());
        var pageResponse = PageResponse.<MessageResponse>builder()
                .content(responses)
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .pageSize(10)
                .first(true)
                .last(true)
                .build();

        when(messageService.getConversation(eq(userId), any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/conversation/" + userId + "?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].senderUsername").value("testUser"))
                .andExpect(jsonPath("$.content[0].receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(messageService, times(1)).getConversation(eq(userId), any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение отправленных сообщений - успешно")
    void getSentMessages_whenRequestIsValid() throws Exception {
        var responses = List.of(TestDataFactory.createMessageResponse());
        var pageResponse = PageResponse.<MessageResponse>builder()
                .content(responses)
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .pageSize(10)
                .first(true)
                .last(true)
                .build();

        when(messageService.getSentMessages(any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/sent?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].senderUsername").value("testUser"))
                .andExpect(jsonPath("$.content[0].receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(messageService, times(1)).getSentMessages(any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение полученных сообщений - успешно")
    void getReceivedMessages_whenRequestIsValid() throws Exception {
        var responses = List.of(TestDataFactory.createMessageResponse());
        var pageResponse = PageResponse.<MessageResponse>builder()
                .content(responses)
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .pageSize(10)
                .first(true)
                .last(true)
                .build();

        when(messageService.getReceivedMessages(any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/received?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].senderUsername").value("testUser"))
                .andExpect(jsonPath("$.content[0].receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(messageService, times(1)).getReceivedMessages(any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение прочитанных сообщений - успешно")
    void getReadMessages_whenRequestIsValid() throws Exception {
        var responses = List.of(TestDataFactory.createMessageResponse());
        var pageResponse = PageResponse.<MessageResponse>builder()
                .content(responses)
                .currentPage(0)
                .totalPages(1)
                .totalElements(1L)
                .pageSize(10)
                .first(true)
                .last(true)
                .build();

        when(messageService.getReadMessages(any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/read?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].senderUsername").value("testUser"))
                .andExpect(jsonPath("$.content[0].receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(messageService, times(1)).getReadMessages(any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование сообщения - успешно")
    void editMessage_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var request = TestDataFactory.createMessageRequest();
        var response = TestDataFactory.createMessageResponse();

        when(messageService.editMessage(eq(messageId), any(), any())).thenReturn(response);

        mockMvcUtils.performPut("/messages/" + messageId, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.content").value("Тестовое сообщение"));

        verify(messageService, times(1)).editMessage(eq(messageId), any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование сообщения - выбрасывает исключение, доступ запрещён")
    void editMessage_whenIsNotOwnerMessage() throws Exception {
        Long messageId = 1L;
        var request = TestDataFactory.createMessageRequest();

        when(messageService.editMessage(eq(messageId), any(), any()))
                .thenThrow(new RuntimeException("Доступ запрещён"));

        mockMvcUtils.performPut("/messages/" + messageId, request)
                .andExpect(status().isInternalServerError());

        verify(messageService, times(1)).editMessage(eq(messageId), any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Отметка сообщения как доставленного - успешно")
    void receiveMessage_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var response = TestDataFactory.createMessageResponse();

        when(messageService.markAsReceived(eq(messageId), any())).thenReturn(response);

        mockMvcUtils.performPatch("/messages/" + messageId + "/receive")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(messageService, times(1)).markAsReceived(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Отметка сообщения как прочитанного - успешно")
    void readMessage_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var response = TestDataFactory.createMessageResponse();

        when(messageService.markAsRead(eq(messageId), any())).thenReturn(response);

        mockMvcUtils.performPatch("/messages/" + messageId + "/read")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(messageService, times(1)).markAsRead(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление сообщения - успешное удаление")
    void deleteMessage_whenRequestIsValid() throws Exception {
        Long messageId = 1L;

        doNothing().when(messageService).deleteMessage(eq(messageId), any());

        mockMvcUtils.performDelete("/messages/" + messageId)
                .andExpect(status().isNoContent());

        verify(messageService, times(1)).deleteMessage(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление сообщения - выбрасывает исключение 'Доступ запрещён'")
    void deleteMessage_whenUserIsNotOwner() throws Exception {
        Long messageId = 1L;

        doThrow(new RuntimeException("Доступ запрещён"))
                .when(messageService).deleteMessage(eq(messageId), any());

        mockMvcUtils.performDelete("/messages/" + messageId)
                .andExpect(status().isInternalServerError());

        verify(messageService, times(1)).deleteMessage(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }
}