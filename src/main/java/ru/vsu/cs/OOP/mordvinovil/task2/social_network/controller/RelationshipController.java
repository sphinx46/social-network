package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.RelationshipService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

@RestController
@RequestMapping("/relationships")
@RequiredArgsConstructor
public class RelationshipController {
    private final RelationshipService relationshipService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(RelationshipController.class);

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового запроса на дружбу")
    @PostMapping("/create")
    public ResponseEntity<RelationshipResponse> sendFriendRequest(
            @Valid @RequestBody RelationshipRequest request) {

        User user = userService.getCurrentUser();
        log.info("Пользователь {} отправляет запрос на дружбу пользователю {}", user.getId(), request.getTargetUserId());
        RelationshipResponse response = relationshipService.sendFriendRequest(request, user);
        log.info("Запрос на дружбу от пользователя {} к пользователю {} успешно создан", user.getId(), request.getTargetUserId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка друзей")
    @GetMapping("/friends")
    public ResponseEntity<PageResponse<RelationshipResponse>> getFriendList(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {

        User user = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает список друзей, страница {}, размер {}", user.getId(), pageNumber, size);
        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();
        PageResponse<RelationshipResponse> pageResponse = relationshipService.getFriendList(user, pageRequest);
        log.info("Получено {} друзей для пользователя {}", pageResponse.getContent().size(), user.getId());
        return ResponseEntity.ok(pageResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение черного списка")
    @GetMapping("/blackList")
    public ResponseEntity<PageResponse<RelationshipResponse>> getBlackList(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {

        User user = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает черный список, страница {}, размер {}", user.getId(), pageNumber, size);
        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();
        PageResponse<RelationshipResponse> pageResponse = relationshipService.getBlackList(user, pageRequest);
        log.info("Получено {} пользователей в черном списке для пользователя {}", pageResponse.getContent().size(), user.getId());
        return ResponseEntity.ok(pageResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка отклоненных запросов на дружбу")
    @GetMapping("/declinedList")
    public ResponseEntity<PageResponse<RelationshipResponse>> getDeclinedList(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {

        User user = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает список отклоненных запросов, страница {}, размер {}", user.getId(), pageNumber, size);
        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();
        PageResponse<RelationshipResponse> pageResponse = relationshipService.getDeclinedList(user, pageRequest);
        log.info("Получено {} отклоненных запросов для пользователя {}", pageResponse.getContent().size(), user.getId());
        return ResponseEntity.ok(pageResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Принять запрос на дружбу")
    @PatchMapping("/acceptFriendRequest")
    public ResponseEntity<RelationshipResponse> acceptFriendRequest(@Valid @RequestBody RelationshipRequest request) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} принимает запрос на дружбу от пользователя {}", user.getId(), request.getTargetUserId());
        RelationshipResponse response = relationshipService.acceptFriendRequest(request, user);
        log.info("Пользователь {} принял запрос на дружбу от пользователя {}", user.getId(), request.getTargetUserId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Отклонить запрос на дружбу")
    @PatchMapping("/declineFriendRequest")
    public ResponseEntity<RelationshipResponse> declineFriendRequest(@Valid @RequestBody RelationshipRequest request) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} отклоняет запрос на дружбу от пользователя {}", user.getId(), request.getTargetUserId());
        RelationshipResponse response = relationshipService.declineFriendRequest(request, user);
        log.info("Пользователь {} отклонил запрос на дружбу от пользователя {}", user.getId(), request.getTargetUserId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Заблокировать друга")
    @PatchMapping("/blockFriend")
    public ResponseEntity<RelationshipResponse> blockFriend(@Valid @RequestBody RelationshipRequest request) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} блокирует пользователя {}", user.getId(), request.getTargetUserId());
        RelationshipResponse response = relationshipService.blockUser(request, user);
        log.info("Пользователь {} заблокировал пользователя {}", user.getId(), request.getTargetUserId());
        return ResponseEntity.ok(response);
    }
}