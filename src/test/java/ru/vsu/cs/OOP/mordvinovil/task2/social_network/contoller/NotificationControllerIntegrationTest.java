package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.NotificationContoller;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.createTestNotificationResponse;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.createTestNotificationResponseList;


@WebMvcTest(NotificationContoller.class)
class NotificationControllerIntegrationTest extends BaseControllerTest {

    @MockitoBean
    private NotificationService notificationService;


    @Test
    @DisplayName("Получение уведомления по ID без авторизации - должно вернуть 401")
    void getNotificationById_whenUnauthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performGet("/notifications/1")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение уведомления по ID - успешно")
    void getNotificationById_whenValidRequest_shouldReturnNotification() throws Exception {
        NotificationResponse response = createTestNotificationResponse();

        when(notificationService.getUserNotificationById(1L, testUser)).thenReturn(response);

        mockMvcUtils.performGet("/notifications/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value("POST_LIKED"))
                .andExpect(jsonPath("$.status").value("UNREAD"))
                .andExpect(jsonPath("$.additionalData.postId").value("123"));

        verify(notificationService, times(1)).getUserNotificationById(1L, testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение уведомления по ID - когда уведомление не найдено")
    void getNotificationById_whenNotificationNotFound_shouldReturnError() throws Exception {
        when(notificationService.getUserNotificationById(999L, testUser))
                .thenThrow(new RuntimeException("Уведомление не найдено"));

        mockMvcUtils.performGet("/notifications/999")
                .andExpect(status().isInternalServerError());

        verify(notificationService, times(1)).getUserNotificationById(999L, testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение всех уведомлений без авторизации - должно вернуть 401")
    void getAllUserNotifications_whenUnauthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performGet("/notifications")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение всех уведомлений - успешно")
    void getAllUserNotifications_whenValidRequest_shouldReturnNotifications() throws Exception {
        List<NotificationResponse> responses = createTestNotificationResponseList();

        when(notificationService.getUserNotifications(testUser)).thenReturn(responses);

        mockMvcUtils.performGet("/notifications")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("POST_LIKED"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].type").value("NEW_MESSAGE"));

        verify(notificationService, times(1)).getUserNotifications(testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение непрочитанных уведомлений без авторизации - должно вернуть 401")
    void getUnreadUserNotifications_whenUnauthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performGet("/notifications/unread")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение непрочитанных уведомлений - успешно")
    void getUnreadUserNotifications_whenValidRequest_shouldReturnUnreadNotifications() throws Exception {
        List<NotificationResponse> responses = createTestNotificationResponseList();

        when(notificationService.getUnreadNotifications(testUser)).thenReturn(responses);

        mockMvcUtils.performGet("/notifications/unread")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("UNREAD"))
                .andExpect(jsonPath("$[1].status").value("UNREAD"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(notificationService, times(1)).getUnreadNotifications(testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение количества непрочитанных уведомлений без авторизации - должно вернуть 401")
    void getCountUnreadNotifications_whenUnauthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performGet("/notifications/countUnread")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение количества непрочитанных уведомлений - успешно")
    void getCountUnreadNotifications_whenValidRequest_shouldReturnCount() throws Exception {
        Long expectedCount = 5L;

        when(notificationService.getUnreadNotificationsCount(testUser)).thenReturn(expectedCount);

        mockMvcUtils.performGet("/notifications/countUnread")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(notificationService, times(1)).getUnreadNotificationsCount(testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Пометка уведомления как прочитанного без авторизации - должно вернуть 401")
    void markNotificationAsReadById_whenUnauthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performPatch("/notifications/markAsRead/1")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Пометка уведомления как прочитанного - успешно")
    void markNotificationAsReadById_whenValidRequest_shouldMarkAsRead() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .type(NotificationType.POST_LIKED)
                .status(NotificationStatus.READ)
                .additionalData(Map.of("postId", "123"))
                .build();

        when(notificationService.markAsRead(1L, testUser)).thenReturn(response);

        mockMvcUtils.performPatch("/notifications/markAsRead/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("READ"));

        verify(notificationService, times(1)).markAsRead(1L, testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Пометка всех уведомлений как прочитанных без авторизации - должно вернуть 401")
    void markNotificationsAllAsRead_whenUnauthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performPatch("/notifications/markAllAsRead")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Пометка всех уведомлений как прочитанных - успешно")
    void markNotificationsAllAsRead_whenValidRequest_shouldMarkAllAsRead() throws Exception {
        mockMvcUtils.performPatch("/notifications/markAllAsRead")
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAllAsRead(testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Удаление уведомления по ID без авторизации - должно вернуть 401")
    void deleteNotificationById_whenUnauthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performDelete("/notifications/1")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление уведомления по ID - успешно")
    void deleteNotificationById_whenValidRequest_shouldDeleteNotification() throws Exception {
        mockMvcUtils.performDelete("/notifications/1")
                .andExpect(status().isOk());

        verify(notificationService, times(1)).deleteNotification(1L, testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление уведомления по ID - когда уведомление не найдено")
    void deleteNotificationById_whenNotificationNotFound_shouldReturnError() throws Exception {
        doThrow(new RuntimeException("Уведомление не найдено"))
                .when(notificationService).deleteNotification(999L, testUser);

        mockMvcUtils.performDelete("/notifications/999")
                .andExpect(status().isInternalServerError());

        verify(notificationService, times(1)).deleteNotification(999L, testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Очистка удаленных уведомлений без авторизации - должно вернуть 401")
    void clearDeletedNotifications_whenUnauthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performDelete("/notifications/clear")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Очистка удаленных уведомлений - успешно")
    void clearDeletedNotifications_whenValidRequest_shouldClearDeleted() throws Exception {
        mockMvcUtils.performDelete("/notifications/clear")
                .andExpect(status().isOk());

        verify(notificationService, times(1)).clearDeletedNotifications(testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Пометка уведомления как прочитанного - когда сервис выбрасывает исключение")
    void markNotificationAsReadById_whenServiceThrowsException_shouldReturnError() throws Exception {
        when(notificationService.markAsRead(1L, testUser))
                .thenThrow(new RuntimeException("Ошибка при обновлении уведомления"));

        mockMvcUtils.performPatch("/notifications/markAsRead/1")
                .andExpect(status().isInternalServerError());

        verify(notificationService, times(1)).markAsRead(1L, testUser);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение всех уведомлений - когда нет уведомлений")
    void getAllUserNotifications_whenNoNotifications_shouldReturnEmptyList() throws Exception {
        when(notificationService.getUserNotifications(testUser)).thenReturn(List.of());

        mockMvcUtils.performGet("/notifications")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(notificationService, times(1)).getUserNotifications(testUser);
        verify(userService, times(1)).getCurrentUser();
    }
}


