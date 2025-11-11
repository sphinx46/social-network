package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.messaging.MessageController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.messaging.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.CacheMode;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.factory.MessageServiceFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging.MessageService;
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
    private MessageServiceFactory messageServiceFactory;


    private final MessageService mockMessageService = mock(MessageService.class);

    @Test
    @DisplayName("Создание сообщения без авторизации - должно вернуть 401")
    void createMessage_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createMessageRequest();

        mockMvcUtils.performPost("/messages/create", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Создание сообщения с кешированием - успешное создание")
    void createMessage_withCache_shouldCreateMessage() throws Exception {
        var request = TestDataFactory.createMessageRequest();
        var response = TestDataFactory.createMessageResponse();

        when(messageServiceFactory.getService(CacheMode.CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.create(any(), any())).thenReturn(response);

        mockMvcUtils.performPost("/messages/create?cacheMode=CACHE", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.senderUsername").value("testUser"))
                .andExpect(jsonPath("$.receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.content").value("Тестовое сообщение"))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(messageServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(mockMessageService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Создание сообщения без кеширования - успешное создание")
    void createMessage_withoutCache_shouldCreateMessage() throws Exception {
        var request = TestDataFactory.createMessageRequest();
        var response = TestDataFactory.createMessageResponse();

        when(messageServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.create(any(), any())).thenReturn(response);

        mockMvcUtils.performPost("/messages/create?cacheMode=NONE_CACHE", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(messageServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(mockMessageService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Создание сообщения - когда сервис выбрасывает исключение")
    void createMessage_whenServiceThrowsException_shouldReturnError() throws Exception {
        var request = TestDataFactory.createMessageRequest();

        when(messageServiceFactory.getService(CacheMode.CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.create(any(), any()))
                .thenThrow(new RuntimeException("Ошибка создания сообщения"));

        mockMvcUtils.performPost("/messages/create?cacheMode=CACHE", request)
                .andExpect(status().isInternalServerError());

        verify(messageServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(mockMessageService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение сообщения по ID с кешированием - успешно")
    void getMessage_withCache_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var response = TestDataFactory.createMessageResponse();

        when(messageServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.getMessageById(eq(messageId), any())).thenReturn(response);

        mockMvcUtils.performGet("/messages/" + messageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.senderUsername").value("testUser"))
                .andExpect(jsonPath("$.receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.content").value("Тестовое сообщение"));

        verify(messageServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(mockMessageService, times(1)).getMessageById(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение сообщения по ID без кеширования - успешно")
    void getMessage_withoutCache_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var response = TestDataFactory.createMessageResponse();

        when(messageServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.getMessageById(eq(messageId), any())).thenReturn(response);

        mockMvcUtils.performGet("/messages/" + messageId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId));

        verify(messageServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(mockMessageService, times(1)).getMessageById(eq(messageId), any());
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
    @DisplayName("Получение переписки с пользователем с кешированием - успешно")
    void getConversation_withCache_whenRequestIsValid() throws Exception {
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

        when(messageServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.getConversation(eq(userId), any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/conversation/" + userId + "?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].senderUsername").value("testUser"))
                .andExpect(jsonPath("$.content[0].receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(messageServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(mockMessageService, times(1)).getConversation(eq(userId), any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение переписки с пользователем без кеширования - успешно")
    void getConversation_withoutCache_whenRequestIsValid() throws Exception {
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

        when(messageServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.getConversation(eq(userId), any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/conversation/" + userId + "?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(messageServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(mockMessageService, times(1)).getConversation(eq(userId), any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение отправленных сообщений с кешированием - успешно")
    void getSentMessages_withCache_whenRequestIsValid() throws Exception {
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

        when(messageServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.getSentMessages(any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/sent?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].senderUsername").value("testUser"))
                .andExpect(jsonPath("$.content[0].receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(messageServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(mockMessageService, times(1)).getSentMessages(any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение полученных сообщений с кешированием - успешно")
    void getReceivedMessages_withCache_whenRequestIsValid() throws Exception {
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

        when(messageServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.getReceivedMessages(any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/received?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].senderUsername").value("testUser"))
                .andExpect(jsonPath("$.content[0].receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(messageServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(mockMessageService, times(1)).getReceivedMessages(any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение прочитанных сообщений с кешированием - успешно")
    void getReadMessages_withCache_whenRequestIsValid() throws Exception {
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

        when(messageServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.getReadMessages(any(), any(PageRequest.class))).thenReturn(pageResponse);

        mockMvcUtils.performGet("/messages/read?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].senderUsername").value("testUser"))
                .andExpect(jsonPath("$.content[0].receiverUsername").value("receiverUser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(messageServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(mockMessageService, times(1)).getReadMessages(any(), any(PageRequest.class));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование сообщения с кешированием - успешно")
    void editMessage_withCache_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var request = TestDataFactory.createMessageRequest();
        var response = TestDataFactory.createMessageResponse();

        when(messageServiceFactory.getService(CacheMode.CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.editMessage(eq(messageId), any(), any())).thenReturn(response);

        mockMvcUtils.performPut("/messages/" + messageId + "?cacheMode=CACHE", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.content").value("Тестовое сообщение"));

        verify(messageServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(mockMessageService, times(1)).editMessage(eq(messageId), any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование сообщения - выбрасывает исключение, доступ запрещён")
    void editMessage_whenIsNotOwnerMessage() throws Exception {
        Long messageId = 1L;
        var request = TestDataFactory.createMessageRequest();

        when(messageServiceFactory.getService(CacheMode.CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.editMessage(eq(messageId), any(), any()))
                .thenThrow(new RuntimeException("Доступ запрещён"));

        mockMvcUtils.performPut("/messages/" + messageId + "?cacheMode=CACHE", request)
                .andExpect(status().isInternalServerError());

        verify(messageServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(mockMessageService, times(1)).editMessage(eq(messageId), any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Отметка сообщения как доставленного с кешированием - успешно")
    void receiveMessage_withCache_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var response = TestDataFactory.createMessageResponse();

        when(messageServiceFactory.getService(CacheMode.CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.markAsReceived(eq(messageId), any())).thenReturn(response);

        mockMvcUtils.performPatch("/messages/" + messageId + "/receive?cacheMode=CACHE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(messageServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(mockMessageService, times(1)).markAsReceived(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Отметка сообщения как прочитанного с кешированием - успешно")
    void readMessage_withCache_whenRequestIsValid() throws Exception {
        Long messageId = 1L;
        var response = TestDataFactory.createMessageResponse();

        when(messageServiceFactory.getService(CacheMode.CACHE)).thenReturn(mockMessageService);
        when(mockMessageService.markAsRead(eq(messageId), any())).thenReturn(response);

        mockMvcUtils.performPatch("/messages/" + messageId + "/read?cacheMode=CACHE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.status").value("SENT"));

        verify(messageServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(mockMessageService, times(1)).markAsRead(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление сообщения с кешированием - успешное удаление")
    void deleteMessage_withCache_whenRequestIsValid() throws Exception {
        Long messageId = 1L;

        when(messageServiceFactory.getService(CacheMode.CACHE)).thenReturn(mockMessageService);
        doNothing().when(mockMessageService).deleteMessage(eq(messageId), any());

        mockMvcUtils.performDelete("/messages/" + messageId + "?cacheMode=CACHE")
                .andExpect(status().isNoContent());

        verify(messageServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(mockMessageService, times(1)).deleteMessage(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление сообщения - выбрасывает исключение 'Доступ запрещён'")
    void deleteMessage_whenUserIsNotOwner() throws Exception {
        Long messageId = 1L;

        when(messageServiceFactory.getService(CacheMode.CACHE)).thenReturn(mockMessageService);
        doThrow(new RuntimeException("Доступ запрещён"))
                .when(mockMessageService).deleteMessage(eq(messageId), any());

        mockMvcUtils.performDelete("/messages/" + messageId + "?cacheMode=CACHE")
                .andExpect(status().isInternalServerError());

        verify(messageServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(mockMessageService, times(1)).deleteMessage(eq(messageId), any());
        verify(userService, times(1)).getCurrentUser();
    }
}

