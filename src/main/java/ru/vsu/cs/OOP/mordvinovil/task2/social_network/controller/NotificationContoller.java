package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationContoller {
    private final NotificationService notificationService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение уведомления по Id")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        NotificationResponse response = notificationService.getUserNotificationById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение всех уведомлений текущего пользователя")
    @GetMapping()
    public ResponseEntity<List<NotificationResponse>> getAllUserNotifications() {
        User currentUser = userService.getCurrentUser();
        List<NotificationResponse> responseList = notificationService.getUserNotifications(currentUser);
        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение непрочитанных уведомлений текущего пользователя")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadUserNotifications() {
        User currentUser = userService.getCurrentUser();
        List<NotificationResponse> responseList = notificationService.getUnreadNotifications(currentUser);
        return ResponseEntity.ok(responseList);
    }


    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение количества непрочитанных уведомлений текущего пользователя")
    @GetMapping("/countUnread")
    public ResponseEntity<Long> getCountUnreadNotifications() {
        User currentUser = userService.getCurrentUser();
        Long count = notificationService.getUnreadNotificationsCount(currentUser);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Пометить как прочитанное")
    @PatchMapping("/markAsRead/{id}")
    public ResponseEntity<NotificationResponse> markNotificationAsReadById(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        NotificationResponse response = notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Пометить все уведомления как прочитанные")
    @PatchMapping("/markAllAsRead")
    public ResponseEntity<Void> markNotificationsAllAsRead() {
        User currentUser = userService.getCurrentUser();
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удалить уведомление по Id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotificationById(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        notificationService.deleteNotification(id, currentUser);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Очистить все удаленные уведомления")
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearDeletedNotifications() {
        User currentUser = userService.getCurrentUser();
        notificationService.clearDeletedNotifications(currentUser);
        return ResponseEntity.ok().build();
    }
}













