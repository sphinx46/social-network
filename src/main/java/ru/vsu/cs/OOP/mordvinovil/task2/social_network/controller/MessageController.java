package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.MessageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService service;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового сообщения")
    @PostMapping("/create")
    public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        MessageResponse response = service.create(request, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить сообщение по ID")
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable Long messageId) {
        User user = userService.getCurrentUser();
        MessageResponse response = service.getMessageById(messageId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить переписку с пользователем")
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<PageResponse<MessageResponse>> getConversation(
            @PathVariable Long userId,
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

        PageResponse<MessageResponse> response = service.getConversation(userId, user, pageRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить отправленные сообщения")
    @GetMapping("/sent")
    public ResponseEntity<PageResponse<MessageResponse>> getSentMessages(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        User user = userService.getCurrentUser();

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<MessageResponse> response = service.getSentMessages(user, pageRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить полученные сообщения")
    @GetMapping("/received")
    public ResponseEntity<PageResponse<MessageResponse>> getReceivedMessages(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        User user = userService.getCurrentUser();

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<MessageResponse> response = service.getReceivedMessages(user, pageRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить прочитанные сообщения")
    @GetMapping("/read")
    public ResponseEntity<PageResponse<MessageResponse>> getReadMessages(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        User user = userService.getCurrentUser();

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<MessageResponse> response = service.getReadMessages(user, pageRequest);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить сообщение")
    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(@PathVariable Long messageId,
                                                       @Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        MessageResponse response = service.editMessage(messageId, request, user);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить статус сообщения на 'Доставлено'")
    @PatchMapping("/{messageId}/receive")
    public ResponseEntity<MessageResponse> receiveMessage(@PathVariable Long messageId) {
        User user = userService.getCurrentUser();
        MessageResponse response = service.markAsReceived(messageId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить статус сообщения на 'Прочитано'")
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<MessageResponse> readMessage(@PathVariable Long messageId) {
        User user = userService.getCurrentUser();
        MessageResponse response = service.markAsRead(messageId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удалить сообщение")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        User user = userService.getCurrentUser();
        service.deleteMessage(messageId, user);
        return ResponseEntity.noContent().build();
    }
}