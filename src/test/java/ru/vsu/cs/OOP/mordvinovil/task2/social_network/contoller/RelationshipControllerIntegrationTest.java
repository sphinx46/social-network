package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.RelationshipController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.RelationshipService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RelationshipController.class)
class RelationshipControllerIntegrationTest extends BaseControllerTest {

    @MockitoBean
    private RelationshipService relationshipService;

    @Test
    @DisplayName("Отправка запроса на дружбу без авторизации - должно вернуть 401")
    void sendFriendRequest_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();

        mockMvcUtils.performPost("/relationships/create", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Отправка запроса на дружбу - успешное создание")
    void sendFriendRequest_whenValidData_shouldCreateRelationship() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();
        var response = TestDataFactory.createRelationshipResponse();

        when(relationshipService.sendFriendRequest(any(), any())).thenReturn(response);

        mockMvcUtils.performPost("/relationships/create", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.senderId").value(1L))
                .andExpect(jsonPath("$.receiverId").value(2L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(relationshipService, times(1)).sendFriendRequest(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Отправка запроса на дружбу - когда сервис выбрасывает исключение")
    void sendFriendRequest_whenServiceThrowsException_shouldReturnError() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();

        when(relationshipService.sendFriendRequest(any(), any()))
                .thenThrow(new RuntimeException("Ошибка создания запроса на дружбу"));

        mockMvcUtils.performPost("/relationships/create", request)
                .andExpect(status().isInternalServerError());

        verify(relationshipService, times(1)).sendFriendRequest(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение списка друзей - успешно")
    void getFriendList_whenRequestIsValid() throws Exception {
        var responses = List.of(TestDataFactory.createRelationshipResponse());

        when(relationshipService.getFriendList(any())).thenReturn(responses);

        mockMvcUtils.performGet("/relationships/friends")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].senderId").value(1L))
                .andExpect(jsonPath("$[0].receiverId").value(2L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(relationshipService, times(1)).getFriendList(any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение списка друзей без авторизации - должно вернуть 401")
    void getFriendList_whenUnAuthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performGet("/relationships/friends")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение черного списка - успешно")
    void getBlackList_whenRequestIsValid() throws Exception {
        var responses = List.of(TestDataFactory.createRelationshipResponse());

        when(relationshipService.getBlackList(any())).thenReturn(responses);

        mockMvcUtils.performGet("/relationships/blackList")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].senderId").value(1L))
                .andExpect(jsonPath("$[0].receiverId").value(2L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(relationshipService, times(1)).getBlackList(any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение списка отклоненных запросов - успешно")
    void getDeclinedList_whenRequestIsValid() throws Exception {
        var responses = List.of(TestDataFactory.createRelationshipResponse());

        when(relationshipService.getDeclinedList(any())).thenReturn(responses);

        mockMvcUtils.performGet("/relationships/declinedList")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].senderId").value(1L))
                .andExpect(jsonPath("$[0].receiverId").value(2L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(relationshipService, times(1)).getDeclinedList(any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Принятие запроса на дружбу - успешно")
    void acceptFriendRequest_whenRequestIsValid() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();
        var response = TestDataFactory.createRelationshipResponse();

        when(relationshipService.acceptFriendRequest(any(), any())).thenReturn(response);

        mockMvcUtils.performPatch("/relationships/acceptFriendRequest", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(relationshipService, times(1)).acceptFriendRequest(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Принятие запроса на дружбу без авторизации - должно вернуть 401")
    void acceptFriendRequest_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();

        mockMvcUtils.performPatch("/relationships/acceptFriendRequest", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Отклонение запроса на дружбу - успешно")
    void declineFriendRequest_whenRequestIsValid() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();
        var response = TestDataFactory.createRelationshipResponse();

        when(relationshipService.declineFriendRequest(any(), any())).thenReturn(response);

        mockMvcUtils.performPatch("/relationships/declineFriendRequest", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(relationshipService, times(1)).declineFriendRequest(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Блокировка пользователя - успешно")
    void blockFriend_whenRequestIsValid() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();
        var response = TestDataFactory.createRelationshipResponse();

        when(relationshipService.blockUser(any(), any())).thenReturn(response);

        mockMvcUtils.performPatch("/relationships/blockFriend", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(relationshipService, times(1)).blockUser(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Принятие запроса на дружбу - выбрасывает исключение, доступ запрещён")
    void acceptFriendRequest_whenAccessDenied() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();

        when(relationshipService.acceptFriendRequest(any(), any()))
                .thenThrow(new RuntimeException("Доступ запрещён"));

        mockMvcUtils.performPatch("/relationships/acceptFriendRequest", request)
                .andExpect(status().isInternalServerError());

        verify(relationshipService, times(1)).acceptFriendRequest(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Отклонение запроса на дружбу - выбрасывает исключение, запрос не существует")
    void declineFriendRequest_whenRequestIsNotExists() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();

        when(relationshipService.declineFriendRequest(any(), any()))
                .thenThrow(new RuntimeException("Запрос не существует"));

        mockMvcUtils.performPatch("/relationships/declineFriendRequest", request)
                .andExpect(status().isInternalServerError());

        verify(relationshipService, times(1)).declineFriendRequest(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Блокировка пользователя - выбрасывает исключение, пользователь не существует")
    void blockFriend_whenUserIsNotExists() throws Exception {
        var request = TestDataFactory.createRelationshipRequest();

        when(relationshipService.blockUser(any(), any()))
                .thenThrow(new RuntimeException("Пользователь не существует"));

        mockMvcUtils.performPatch("/relationships/blockFriend", request)
                .andExpect(status().isInternalServerError());

        verify(relationshipService, times(1)).blockUser(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }
}