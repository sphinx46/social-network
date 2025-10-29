package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение уведомления по Id")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает уведомление {}", currentUser.getId(), id);
        NotificationResponse response = notificationService.getUserNotificationById(id, currentUser);
        log.info("Уведомление {} успешно получено пользователем {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение всех уведомлений текущего пользователя")
    @GetMapping()
    public ResponseEntity<PageResponse<NotificationResponse>> getAllUserNotifications(
            @RequestParam(defaultValue = "5", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает все уведомления, страница {}, размер {}", currentUser.getId(), pageNumber, size);

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<NotificationResponse> responsePage =
                notificationService.getUserNotifications(currentUser, pageRequest);
        log.info("Получено {} уведомлений для пользователя {}", responsePage.getContent().size(), currentUser.getId());
        return ResponseEntity.ok(responsePage);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение непрочитанных уведомлений текущего пользователя")
    @GetMapping("/unread")
    public ResponseEntity<PageResponse<NotificationResponse>> getUnreadUserNotifications(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает непрочитанные уведомления, страница {}, размер {}", currentUser.getId(), pageNumber, size);

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<NotificationResponse> responsePage =
                notificationService.getUnreadNotifications(currentUser, pageRequest);
        log.info("Получено {} непрочитанных уведомлений для пользователя {}", responsePage.getContent().size(), currentUser.getId());
        return ResponseEntity.ok(responsePage);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение количества непрочитанных уведомлений текущего пользователя")
    @GetMapping("/countUnread")
    public ResponseEntity<Long> getCountUnreadNotifications() {
        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает количество непрочитанных уведомлений", currentUser.getId());
        Long count = notificationService.getUnreadNotificationsCount(currentUser);
        log.info("Пользователь {} имеет {} непрочитанных уведомлений", currentUser.getId(), count);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Пометить как прочитанное")
    @PatchMapping("/markAsRead/{id}")
    public ResponseEntity<NotificationResponse> markNotificationAsReadById(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} помечает уведомление {} как прочитанное", currentUser.getId(), id);
        NotificationResponse response = notificationService.markAsRead(id, currentUser);
        log.info("Уведомление {} помечено как прочитанное пользователем {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Пометить все уведомления как прочитанные")
    @PatchMapping("/markAllAsRead")
    public ResponseEntity<Void> markNotificationsAllAsRead() {
        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} помечает все уведомления как прочитанные", currentUser.getId());
        notificationService.markAllAsRead(currentUser);
        log.info("Все уведомления пользователя {} помечены как прочитанные", currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удалить уведомление по Id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotificationById(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} удаляет уведомление {}", currentUser.getId(), id);
        notificationService.deleteNotification(id, currentUser);
        log.info("Уведомление {} успешно удалено пользователем {}", id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Очистить все удаленные уведомления")
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearDeletedNotifications() {
        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} очищает удаленные уведомления", currentUser.getId());
        notificationService.clearDeletedNotifications(currentUser);
        log.info("Удаленные уведомления пользователя {} успешно очищены", currentUser.getId());
        return ResponseEntity.ok().build();
    }
}