package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.notification;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.notification.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.notification.NotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение уведомления по Id")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("notificationId", id);

        centralLogger.logInfo("УВЕДОМЛЕНИЕ_ПОЛУЧЕНИЕ_ЗАПРОС",
                "Запрос на получение уведомления по ID", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            NotificationResponse response = notificationService.getUserNotificationById(id, currentUser);

            centralLogger.logInfo("УВЕДОМЛЕНИЕ_ПОЛУЧЕНО",
                    "Уведомление успешно получено", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("УВЕДОМЛЕНИЕ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении уведомления", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ВСЕ_УВЕДОМЛЕНИЯ_ЗАПРОС",
                "Запрос всех уведомлений пользователя", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<NotificationResponse> responsePage =
                    notificationService.getUserNotifications(currentUser, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", responsePage.getContent().size());
            successContext.put("totalElements", responsePage.getTotalElements());

            centralLogger.logInfo("ВСЕ_УВЕДОМЛЕНИЯ_ПОЛУЧЕНЫ",
                    "Все уведомления успешно получены", successContext);

            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            centralLogger.logError("ВСЕ_УВЕДОМЛЕНИЯ_ОШИБКА",
                    "Ошибка при получении всех уведомлений", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("НЕПРОЧИТАННЫЕ_УВЕДОМЛЕНИЯ_ЗАПРОС",
                "Запрос непрочитанных уведомлений", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<NotificationResponse> responsePage =
                    notificationService.getUnreadNotifications(currentUser, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", responsePage.getContent().size());
            successContext.put("totalElements", responsePage.getTotalElements());

            centralLogger.logInfo("НЕПРОЧИТАННЫЕ_УВЕДОМЛЕНИЯ_ПОЛУЧЕНЫ",
                    "Непрочитанные уведомления успешно получены", successContext);

            return ResponseEntity.ok(responsePage);
        } catch (Exception e) {
            centralLogger.logError("НЕПРОЧИТАННЫЕ_УВЕДОМЛЕНИЯ_ОШИБКА",
                    "Ошибка при получении непрочитанных уведомлений", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение количества непрочитанных уведомлений текущего пользователя")
    @GetMapping("/countUnread")
    public ResponseEntity<Long> getCountUnreadNotifications() {
        Map<String, Object> context = new HashMap<>();

        centralLogger.logInfo("КОЛИЧЕСТВО_НЕПРОЧИТАННЫХ_УВЕДОМЛЕНИЙ_ЗАПРОС",
                "Запрос количества непрочитанных уведомлений", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            Long count = notificationService.getUnreadNotificationsCount(currentUser);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("count", count);

            centralLogger.logInfo("КОЛИЧЕСТВО_НЕПРОЧИТАННЫХ_УВЕДОМЛЕНИЙ_ПОЛУЧЕНО",
                    "Количество непрочитанных уведомлений получено", successContext);

            return ResponseEntity.ok(count);
        } catch (Exception e) {
            centralLogger.logError("КОЛИЧЕСТВО_НЕПРОЧИТАННЫХ_УВЕДОМЛЕНИЙ_ОШИБКА",
                    "Ошибка при получении количества непрочитанных уведомлений", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Пометить как прочитанное")
    @PatchMapping("/markAsRead/{id}")
    public ResponseEntity<NotificationResponse> markNotificationAsReadById(@PathVariable Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("notificationId", id);

        centralLogger.logInfo("УВЕДОМЛЕНИЕ_ПРОЧТЕНИЕ_ЗАПРОС",
                "Запрос на пометку уведомления как прочитанного", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            NotificationResponse response = notificationService.markAsRead(id, currentUser);

            centralLogger.logInfo("УВЕДОМЛЕНИЕ_ПРОЧИТАНО",
                    "Уведомление помечено как прочитанное", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("УВЕДОМЛЕНИЕ_ОШИБКА_ПРОЧТЕНИЯ",
                    "Ошибка при пометке уведомления как прочитанного", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Пометить все уведомления как прочитанные")
    @PatchMapping("/markAllAsRead")
    public ResponseEntity<Void> markNotificationsAllAsRead() {
        Map<String, Object> context = new HashMap<>();

        centralLogger.logInfo("ВСЕ_УВЕДОМЛЕНИЯ_ПРОЧТЕНИЕ_ЗАПРОС",
                "Запрос на пометку всех уведомлений как прочитанных", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            notificationService.markAllAsRead(currentUser);

            centralLogger.logInfo("ВСЕ_УВЕДОМЛЕНИЯ_ПРОЧИТАНЫ",
                    "Все уведомления помечены как прочитанные", context);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            centralLogger.logError("ВСЕ_УВЕДОМЛЕНИЯ_ОШИБКА_ПРОЧТЕНИЯ",
                    "Ошибка при пометке всех уведомлений как прочитанных", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удалить уведомление по Id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotificationById(@PathVariable Long id) {
        Map<String, Object> context = new HashMap<>();
        context.put("notificationId", id);

        centralLogger.logInfo("УВЕДОМЛЕНИЕ_УДАЛЕНИЕ_ЗАПРОС",
                "Запрос на удаление уведомления", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            notificationService.deleteNotification(id, currentUser);

            centralLogger.logInfo("УВЕДОМЛЕНИЕ_УДАЛЕНО",
                    "Уведомление успешно удалено", context);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            centralLogger.logError("УВЕДОМЛЕНИЕ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении уведомления", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Очистить все удаленные уведомления")
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearDeletedNotifications() {
        Map<String, Object> context = new HashMap<>();

        centralLogger.logInfo("ОЧИСТКА_УДАЛЕННЫХ_УВЕДОМЛЕНИЙ_ЗАПРОС",
                "Запрос на очистку удаленных уведомлений", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            notificationService.clearDeletedNotifications(currentUser);

            centralLogger.logInfo("УДАЛЕННЫЕ_УВЕДОМЛЕНИЙ_ОЧИЩЕНЫ",
                    "Удаленные уведомления успешно очищены", context);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            centralLogger.logError("ОЧИСТКА_УДАЛЕННЫХ_УВЕДОМЛЕНИЙ_ОШИБКА",
                    "Ошибка при очистке удаленных уведомлений", context, e);
            throw e;
        }
    }
}