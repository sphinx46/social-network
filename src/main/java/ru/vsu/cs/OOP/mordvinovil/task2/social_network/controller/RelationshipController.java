package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.RelationshipService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

import java.util.List;

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
    public ResponseEntity<List<RelationshipResponse>> getFriendList() {

        User user = userService.getCurrentUser();
        List<RelationshipResponse> listResponses = relationshipService.getFriendList(user);
        return ResponseEntity.ok(listResponses);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение черного списка")
    @GetMapping("/blackList")
    public ResponseEntity<List<RelationshipResponse>> getBlackList() {
        User user = userService.getCurrentUser();
        List<RelationshipResponse> listResponses = relationshipService.getBlackList(user);
        return ResponseEntity.ok(listResponses);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка отклоненных запросов на дружбу")
    @GetMapping("/declinedList")
    public ResponseEntity<List<RelationshipResponse>> getDeclinedList() {
        User user = userService.getCurrentUser();
        List<RelationshipResponse> listResponses = relationshipService.getDeclinedList(user);
        return ResponseEntity.ok(listResponses);
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
