package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.messaging;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.messaging.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.messaging.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.CacheMode;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.factory.MessageServiceFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging.MessageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final UserService userService;
    private final MessageServiceFactory messageServiceFactory;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового сообщения")
    @PostMapping("/create")
    public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody MessageRequest request,
                                                         @RequestParam(value = "cacheMode", defaultValue = "CACHE") CacheMode cacheMode) {
        Map<String, Object> context = new HashMap<>();
        context.put("receiverUserId", request.getReceiverUserId());
        context.put("cacheMode", cacheMode);

        centralLogger.logInfo("СООБЩЕНИЕ_СОЗДАНИЕ_ЗАПРОС",
                "Запрос на создание сообщения", context);

        try {
            User user = userService.getCurrentUser();
            context.put("senderUserId", user.getId());

            MessageService messageService = messageServiceFactory.getService(cacheMode);
            MessageResponse response = messageService.create(request, user);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("messageId", response.getId());

            centralLogger.logInfo("СООБЩЕНИЕ_СОЗДАНО",
                    "Сообщение успешно создано", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании сообщения", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить сообщение по ID")
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable Long messageId) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);

        centralLogger.logInfo("СООБЩЕНИЕ_ПОЛУЧЕНИЕ_ЗАПРОС",
                "Запрос на получение сообщения по ID", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            MessageService service = messageServiceFactory.getService(CacheMode.NONE_CACHE);
            MessageResponse response = service.getMessageById(messageId, user);

            centralLogger.logInfo("СООБЩЕНИЕ_ПОЛУЧЕНО",
                    "Сообщение успешно получено", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении сообщения", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить переписку с пользователем")
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<PageResponse<MessageResponse>> getConversation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", userId);
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);
        context.put("cacheMode", cacheMode);

        centralLogger.logInfo("ПЕРЕПИСКА_ЗАПРОС",
                "Запрос переписки с пользователем", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            MessageService messageService = messageServiceFactory.getService(cacheMode);
            PageResponse<MessageResponse> response = messageService.getConversation(userId, user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", response.getContent().size());
            successContext.put("totalElements", response.getTotalElements());

            centralLogger.logInfo("ПЕРЕПИСКА_ПОЛУЧЕНА",
                    "Переписка успешно получена", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПЕРЕПИСКА_ОШИБКА",
                    "Ошибка при получении переписки", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ОТПРАВЛЕННЫЕ_СООБЩЕНИЯ_ЗАПРОС",
                "Запрос отправленных сообщений", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            MessageService service = messageServiceFactory.getService(CacheMode.NONE_CACHE);
            PageResponse<MessageResponse> response = service.getSentMessages(user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", response.getContent().size());
            successContext.put("totalElements", response.getTotalElements());

            centralLogger.logInfo("ОТПРАВЛЕННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНЫ",
                    "Отправленные сообщения успешно получены", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ОТПРАВЛЕННЫЕ_СООБЩЕНИЯ_ОШИБКА",
                    "Ошибка при получении отправленных сообщений", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ПОЛУЧЕННЫЕ_СООБЩЕНИЯ_ЗАПРОС",
                "Запрос полученных сообщений", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            MessageService service = messageServiceFactory.getService(CacheMode.NONE_CACHE);
            PageResponse<MessageResponse> response = service.getReceivedMessages(user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", response.getContent().size());
            successContext.put("totalElements", response.getTotalElements());

            centralLogger.logInfo("ПОЛУЧЕННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНЫ",
                    "Полученные сообщения успешно получены", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПОЛУЧЕННЫЕ_СООБЩЕНИЯ_ОШИБКА",
                    "Ошибка при получении полученных сообщений", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ПРОЧИТАННЫЕ_СООБЩЕНИЯ_ЗАПРОС",
                "Запрос прочитанных сообщений", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            MessageService service = messageServiceFactory.getService(CacheMode.NONE_CACHE);
            PageResponse<MessageResponse> response = service.getReadMessages(user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", response.getContent().size());
            successContext.put("totalElements", response.getTotalElements());

            centralLogger.logInfo("ПРОЧИТАННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНЫ",
                    "Прочитанные сообщения успешно получены", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПРОЧИТАННЫЕ_СООБЩЕНИЯ_ОШИБКА",
                    "Ошибка при получении прочитанных сообщений", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить сообщение")
    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(@PathVariable Long messageId,
                                                       @Valid @RequestBody MessageRequest request,
                                                       @RequestParam(value = "cacheMode", defaultValue = "CACHE") CacheMode cacheMode) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("cacheMode", cacheMode);

        centralLogger.logInfo("СООБЩЕНИЕ_РЕДАКТИРОВАНИЕ_ЗАПРОС",
                "Запрос на редактирование сообщения", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            MessageService messageService = messageServiceFactory.getService(cacheMode);
            MessageResponse response = messageService.editMessage(messageId, request, user);

            centralLogger.logInfo("СООБЩЕНИЕ_ОТРЕДАКТИРОВАНО",
                    "Сообщение успешно отредактировано", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_РЕДАКТИРОВАНИЯ",
                    "Ошибка при редактировании сообщения", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить статус сообщения на 'Доставлено'")
    @PatchMapping("/{messageId}/receive")
    public ResponseEntity<MessageResponse> receiveMessage(@PathVariable Long messageId,
                                                          @RequestParam(value = "cacheMode", defaultValue = "CACHE") CacheMode cacheMode) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("cacheMode", cacheMode);

        centralLogger.logInfo("СООБЩЕНИЕ_ДОСТАВКА_ЗАПРОС",
                "Запрос на отметку сообщения как доставленного", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            MessageService messageService = messageServiceFactory.getService(cacheMode);
            MessageResponse response = messageService.markAsReceived(messageId, user);

            centralLogger.logInfo("СООБЩЕНИЕ_ДОСТАВЛЕНО",
                    "Сообщение отмечено как доставленное", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_ДОСТАВКИ",
                    "Ошибка при отметке сообщения как доставленного", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Изменить статус сообщения на 'Прочитано'")
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<MessageResponse> readMessage(@PathVariable Long messageId,
                                                       @RequestParam(value = "cacheMode", defaultValue = "CACHE") CacheMode cacheMode) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("cacheMode", cacheMode);

        centralLogger.logInfo("СООБЩЕНИЕ_ПРОЧТЕНИЕ_ЗАПРОС",
                "Запрос на отметку сообщения как прочитанного", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            MessageService messageService = messageServiceFactory.getService(cacheMode);
            MessageResponse response = messageService.markAsRead(messageId, user);

            centralLogger.logInfo("СООБЩЕНИЕ_ПРОЧИТАНО",
                    "Сообщение отмечено как прочитанное", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_ПРОЧТЕНИЯ",
                    "Ошибка при отметке сообщения как прочитанного", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удалить сообщение")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId,
                                              @RequestParam(value = "cacheMode", defaultValue = "CACHE") CacheMode cacheMode) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("cacheMode", cacheMode);

        centralLogger.logInfo("СООБЩЕНИЕ_УДАЛЕНИЕ_ЗАПРОС",
                "Запрос на удаление сообщения", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            MessageService messageService = messageServiceFactory.getService(cacheMode);
            messageService.deleteMessage(messageId, user);

            centralLogger.logInfo("СООБЩЕНИЕ_УДАЛЕНО",
                    "Сообщение успешно удалено", context);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении сообщения", context, e);
            throw e;
        }
    }
}