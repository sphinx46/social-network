package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.relationship;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.relationship.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.relationship.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.relationship.RelationshipService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/relationships")
@RequiredArgsConstructor
public class RelationshipController {
    private final RelationshipService relationshipService;
    private final UserService userService;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового запроса на дружбу")
    @PostMapping("/create")
    public ResponseEntity<RelationshipResponse> sendFriendRequest(
            @Valid @RequestBody RelationshipRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", request.getTargetUserId());

        centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_СОЗДАНИЕ_ЗАПРОС",
                "Запрос на создание запроса на дружбу", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            RelationshipResponse response = relationshipService.sendFriendRequest(request, user);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("relationshipId", response.getId());

            centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_СОЗДАН",
                    "Запрос на дружбу успешно создан", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ЗАПРОС_ДРУЖБЫ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании запроса на дружбу", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка друзей")
    @GetMapping("/friends")
    public ResponseEntity<PageResponse<RelationshipResponse>> getFriendList(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("СПИСОК_ДРУЗЕЙ_ЗАПРОС",
                "Запрос списка друзей", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<RelationshipResponse> pageResponse = relationshipService.getFriendList(user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", pageResponse.getContent().size());
            successContext.put("totalElements", pageResponse.getTotalElements());

            centralLogger.logInfo("СПИСОК_ДРУЗЕЙ_ПОЛУЧЕН",
                    "Список друзей успешно получен", successContext);

            return ResponseEntity.ok(pageResponse);
        } catch (Exception e) {
            centralLogger.logError("СПИСОК_ДРУЗЕЙ_ОШИБКА",
                    "Ошибка при получении списка друзей", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение черного списка")
    @GetMapping("/blackList")
    public ResponseEntity<PageResponse<RelationshipResponse>> getBlackList(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ЧЕРНЫЙ_СПИСОК_ЗАПРОС",
                "Запрос черного списка", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<RelationshipResponse> pageResponse = relationshipService.getBlackList(user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", pageResponse.getContent().size());
            successContext.put("totalElements", pageResponse.getTotalElements());

            centralLogger.logInfo("ЧЕРНЫЙ_СПИСОК_ПОЛУЧЕН",
                    "Черный список успешно получен", successContext);

            return ResponseEntity.ok(pageResponse);
        } catch (Exception e) {
            centralLogger.logError("ЧЕРНЫЙ_СПИСОК_ОШИБКА",
                    "Ошибка при получении черного списка", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка отклоненных запросов на дружбу")
    @GetMapping("/declinedList")
    public ResponseEntity<PageResponse<RelationshipResponse>> getDeclinedList(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ОТКЛОНЕННЫЕ_ЗАПРОСЫ_ЗАПРОС",
                "Запрос списка отклоненных запросов на дружбу", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<RelationshipResponse> pageResponse = relationshipService.getDeclinedList(user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", pageResponse.getContent().size());
            successContext.put("totalElements", pageResponse.getTotalElements());

            centralLogger.logInfo("ОТКЛОНЕННЫЕ_ЗАПРОСЫ_ПОЛУЧЕНЫ",
                    "Список отклоненных запросов успешно получен", successContext);

            return ResponseEntity.ok(pageResponse);
        } catch (Exception e) {
            centralLogger.logError("ОТКЛОНЕННЫЕ_ЗАПРОСЫ_ОШИБКА",
                    "Ошибка при получении списка отклоненных запросов", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Принять запрос на дружбу")
    @PatchMapping("/acceptFriendRequest")
    public ResponseEntity<RelationshipResponse> acceptFriendRequest(@Valid @RequestBody RelationshipRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", request.getTargetUserId());

        centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ПРИНЯТИЕ_ЗАПРОС",
                "Запрос на принятие запроса на дружбу", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            RelationshipResponse response = relationshipService.acceptFriendRequest(request, user);

            centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ПРИНЯТ",
                    "Запрос на дружбу успешно принят", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ЗАПРОС_ДРУЖБЫ_ОШИБКА_ПРИНЯТИЯ",
                    "Ошибка при принятии запроса на дружбу", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Отклонить запрос на дружбу")
    @PatchMapping("/declineFriendRequest")
    public ResponseEntity<RelationshipResponse> declineFriendRequest(@Valid @RequestBody RelationshipRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", request.getTargetUserId());

        centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ОТКЛОНЕНИЕ_ЗАПРОС",
                "Запрос на отклонение запроса на дружбу", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            RelationshipResponse response = relationshipService.declineFriendRequest(request, user);

            centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ОТКЛОНЕН",
                    "Запрос на дружбу успешно отклонен", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ЗАПРОС_ДРУЖБЫ_ОШИБКА_ОТКЛОНЕНИЯ",
                    "Ошибка при отклонении запроса на дружбу", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Заблокировать друга")
    @PatchMapping("/blockFriend")
    public ResponseEntity<RelationshipResponse> blockFriend(@Valid @RequestBody RelationshipRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", request.getTargetUserId());

        centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_БЛОКИРОВКА_ЗАПРОС",
                "Запрос на блокировку пользователя", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            RelationshipResponse response = relationshipService.blockUser(request, user);

            centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_ЗАБЛОКИРОВАН",
                    "Пользователь успешно заблокирован", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПОЛЬЗОВАТЕЛЬ_ОШИБКА_БЛОКИРОВКИ",
                    "Ошибка при блокировке пользователя", context, e);
            throw e;
        }
    }
}