package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.MessageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

import java.util.List;

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
    public ResponseEntity<List<MessageResponse>> getConversation(@PathVariable Long userId) {
        User user = userService.getCurrentUser();
        List<MessageResponse> response = service.getConversation(userId, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить отправленные сообщения")
    @GetMapping("/sent")
    public ResponseEntity<List<MessageResponse>> getSentMessages() {
        User user = userService.getCurrentUser();
        List<MessageResponse> response = service.getSentMessages(user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить полученные сообщения")
    @GetMapping("/received")
    public ResponseEntity<List<MessageResponse>> getReceivedMessages() {
        User user = userService.getCurrentUser();
        List<MessageResponse> response = service.getReceivedMessages(user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить прочитанные сообщения")
    @GetMapping("/read")
    public ResponseEntity<List<MessageResponse>> getReadMessages() {
        User user = userService.getCurrentUser();
        List<MessageResponse> response = service.getReadMessages(user);
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