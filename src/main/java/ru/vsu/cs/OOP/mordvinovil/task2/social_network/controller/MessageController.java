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
    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового сообщения")
    @PostMapping("/create")
    public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} создает сообщение для пользователя {}", user.getId(), request.getReceiverUserId());
        MessageResponse response = service.create(request, user);
        log.info("Сообщение {} успешно создано пользователем {}", response.getId(), user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить сообщение по ID")
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable Long messageId) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает сообщение {}", user.getId(), messageId);
        MessageResponse response = service.getMessageById(messageId, user);
        log.info("Сообщение {} успешно получено пользователем {}", messageId, user.getId());
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
        log.info("Пользователь {} запрашивает переписку с пользователем {}, страница {}, размер {}", user.getId(), userId, pageNumber, size);

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<MessageResponse> response = service.getConversation(userId, user, pageRequest);
        log.info("Получено {} сообщений в переписке пользователя {} с пользователем {}", response.getContent().size(), user.getId(), userId);
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
        log.info("Пользователь {} запрашивает отправленные сообщения, страница {}, размер {}", user.getId(), pageNumber, size);

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<MessageResponse> response = service.getSentMessages(user, pageRequest);
        log.info("Получено {} отправленных сообщений пользователя {}", response.getContent().size(), user.getId());
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
        log.info("Пользователь {} запрашивает полученные сообщения, страница {}, размер {}", user.getId(), pageNumber, size);

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<MessageResponse> response = service.getReceivedMessages(user, pageRequest);
        log.info("Получено {} полученных сообщений пользователя {}", response.getContent().size(), user.getId());
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
        log.info("Пользователь {} запрашивает прочитанные сообщения, страница {}, размер {}", user.getId(), pageNumber, size);

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<MessageResponse> response = service.getReadMessages(user, pageRequest);
        log.info("Получено {} прочитанных сообщений пользователя {}", response.getContent().size(), user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить сообщение")
    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(@PathVariable Long messageId,
                                                       @Valid @RequestBody MessageRequest request) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} редактирует сообщение {}", user.getId(), messageId);
        MessageResponse response = service.editMessage(messageId, request, user);
        log.info("Сообщение {} успешно отредактировано пользователем {}", messageId, user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить статус сообщения на 'Доставлено'")
    @PatchMapping("/{messageId}/receive")
    public ResponseEntity<MessageResponse> receiveMessage(@PathVariable Long messageId) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} отмечает сообщение {} как доставленное", user.getId(), messageId);
        MessageResponse response = service.markAsReceived(messageId, user);
        log.info("Сообщение {} отмечено как доставленное пользователем {}", messageId, user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить статус сообщения на 'Прочитано'")
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<MessageResponse> readMessage(@PathVariable Long messageId) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} отмечает сообщение {} как прочитанное", user.getId(), messageId);
        MessageResponse response = service.markAsRead(messageId, user);
        log.info("Сообщение {} отмечено как прочитанное пользователем {}", messageId, user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удалить сообщение")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} удаляет сообщение {}", user.getId(), messageId);
        service.deleteMessage(messageId, user);
        log.info("Сообщение {} успешно удалено пользователем {}", messageId, user.getId());
        return ResponseEntity.noContent().build();
    }
}