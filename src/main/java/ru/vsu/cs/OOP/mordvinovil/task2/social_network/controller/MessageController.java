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
    public ResponseEntity<MessageResponse> createMessage (
            @Valid @RequestBody MessageRequest request) {

        MessageResponse response = service.create(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить статус сообщения на 'Доставлено'")
    @PatchMapping("/updateStatus/receive")
    public ResponseEntity<List<MessageResponse>> receiveMessage(@Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        List<MessageResponse> response = service.receiveMessages(request, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить статус сообщения на 'Прочитано'")
    @PatchMapping("/updateStatus/read")
    public ResponseEntity<List<MessageResponse>> readMessage(@Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        List<MessageResponse> responseList = service.readMessages(request, user);
        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка отправленных сообщений")
    @GetMapping("/sent")
    public ResponseEntity<List<MessageResponse>> getSentMessages(@Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        List<MessageResponse> responseList = service.getSentMessages(request, user);
        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка доставленных сообщений")
    @GetMapping("/received")
    public ResponseEntity<List<MessageResponse>> getReceivedMessages(@Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        List<MessageResponse> responseList = service.getReceivedMessages(request, user);
        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка прочитанных сообщений")
    @GetMapping("/read")
    public ResponseEntity<List<MessageResponse>> getReadMessages(@Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        List<MessageResponse> responseList = service.getReadMessages(request, user);
        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение переписки между двумя пользователями")
    @GetMapping("/messages")
    public ResponseEntity<List<MessageResponse>> getMessageListBetweenUsers(@Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        List<MessageResponse> responseList = service.getMessageListBetweenUsers(request, user);
        return ResponseEntity.ok(responseList);
    }
}
