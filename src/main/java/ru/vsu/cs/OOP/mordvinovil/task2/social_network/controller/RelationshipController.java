package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового запроса на дружбу")
    @PostMapping("/create")
    public ResponseEntity<RelationshipResponse> sendFriendRequest(
            @Valid @RequestBody RelationshipRequest request) {

        User user = userService.getCurrentUser();
        RelationshipResponse response = relationshipService.sendFriendRequest(request, user);
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
        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();
        PageResponse<RelationshipResponse> pageResponse = relationshipService.getFriendList(user, pageRequest);
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
        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();
        PageResponse<RelationshipResponse> pageResponse = relationshipService.getBlackList(user, pageRequest);
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
        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();
        PageResponse<RelationshipResponse> pageResponse = relationshipService.getDeclinedList(user, pageRequest);
        return ResponseEntity.ok(pageResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Принять запрос на дружбу")
    @PatchMapping("/acceptFriendRequest")
    public ResponseEntity<RelationshipResponse> acceptFriendRequest(@Valid @RequestBody RelationshipRequest request) {
        User user = userService.getCurrentUser();
        RelationshipResponse response = relationshipService.acceptFriendRequest(request, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Отклонить запрос на дружбу")
    @PatchMapping("/declineFriendRequest")
    public ResponseEntity<RelationshipResponse> declineFriendRequest(@Valid @RequestBody RelationshipRequest request) {
        User user = userService.getCurrentUser();
        RelationshipResponse response = relationshipService.declineFriendRequest(request, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Заблокировать друга")
    @PatchMapping("/blockFriend")
    public ResponseEntity<RelationshipResponse> blockFriend(@Valid @RequestBody RelationshipRequest request) {
        User user = userService.getCurrentUser();
        RelationshipResponse response = relationshipService.blockUser(request, user);
        return ResponseEntity.ok(response);
    }
}