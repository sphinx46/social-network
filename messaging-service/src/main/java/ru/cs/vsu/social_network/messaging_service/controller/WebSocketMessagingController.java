package ru.cs.vsu.social_network.messaging_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.websocket.TypingIndicator;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.service.WebSocketMessagingService;

import java.util.UUID;

/**
 * Контроллер для управления WebSocket уведомлениями в мессенджере.
 * Предоставляет REST API для работы с уведомлениями в реальном времени.
 * Все операции требуют аутентификации через API Gateway.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/websocket-messaging")
public class WebSocketMessagingController {

    private final WebSocketMessagingService webSocketMessagingService;

    /**
     * Отправляет новое сообщение с WebSocket уведомлением.
     *
     * @param senderId идентификатор отправителя из заголовка
     * @param messageCreateRequest DTO с данными для создания сообщения
     * @return отправленное сообщение с уведомлением
     */
    @Operation(summary = "Отправка сообщения с WebSocket уведомлением")
    @PostMapping("/send-with-notification")
    public ResponseEntity<MessageResponse> sendMessageWithNotification(
            @RequestHeader("X-User-Id") final UUID senderId,
            @Valid @RequestBody final MessageCreateRequest messageCreateRequest) {

        log.info("WEBSOCKET_КОНТРОЛЛЕР_ОТПРАВКА_С_УВЕДОМЛЕНИЕМ_НАЧАЛО: " +
                        "отправка сообщения с уведомлением от {} к {}, длина контента: {}",
                senderId, messageCreateRequest.getReceiverId(),
                messageCreateRequest.getContent().length());

        final MessageResponse response = webSocketMessagingService
                .sendMessageWithNotification(senderId, messageCreateRequest);

        log.info("WEBSOCKET_КОНТРОЛЛЕР_ОТПРАВКА_С_УВЕДОМЛЕНИЕМ_УСПЕХ: " +
                        "сообщение отправлено с ID: {} и WebSocket уведомлением от {} к {}",
                response.getMessageId(), senderId, messageCreateRequest.getReceiverId());
        return ResponseEntity.ok(response);
    }

    /**
     * Отмечает беседу как прочитанную с WebSocket уведомлением.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param conversationId идентификатор беседы
     * @return количество отмеченных сообщений
     */
    @Operation(summary = "Отметка беседы как прочитанной с WebSocket уведомлением")
    @PostMapping("/conversation/{conversationId}/read-with-notification")
    public ResponseEntity<Integer> markConversationAsReadWithNotification(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @PathVariable("conversationId") final UUID conversationId) {

        log.info("WEBSOCKET_КОНТРОЛЛЕР_ОТМЕТКА_ПРОЧИТАННОЙ_С_УВЕДОМЛЕНИЕМ_НАЧАЛО: " +
                "отметка беседы {} как прочитанной с уведомлением пользователем {}", conversationId, userId);

        final int markedCount = webSocketMessagingService
                .markConversationAsReadWithNotification(userId, conversationId);

        log.info("WEBSOCKET_КОНТРОЛЛЕР_ОТМЕТКА_ПРОЧИТАННОЙ_С_УВЕДОМЛЕНИЕМ_УСПЕХ: " +
                        "отмечено {} сообщений как прочитанные с WebSocket уведомлением в беседе {}",
                markedCount, conversationId);
        return ResponseEntity.ok(markedCount);
    }

    /**
     * Загружает изображение для сообщения с WebSocket уведомлением.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param request DTO с данными для загрузки изображения
     * @return обновленное сообщение с уведомлением
     */
    @Operation(summary = "Загрузка изображения для сообщения с WebSocket уведомлением")
    @PostMapping("/message/image/upload-with-notification")
    public ResponseEntity<MessageResponse> uploadMessageImageWithNotification(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @RequestBody final MessageUploadImageRequest request) {

        log.info("WEBSOCKET_КОНТРОЛЛЕР_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_С_УВЕДОМЛЕНИЕМ_НАЧАЛО: " +
                        "загрузка изображения для сообщения {} с уведомлением пользователем {}",
                request.getMessageId(), userId);

        final MessageResponse response = webSocketMessagingService
                .uploadMessageImageWithNotification(userId, request);

        log.info("WEBSOCKET_КОНТРОЛЛЕР_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_С_УВЕДОМЛЕНИЕМ_УСПЕХ: " +
                        "изображение загружено для сообщения {} с WebSocket уведомлением",
                request.getMessageId());
        return ResponseEntity.ok(response);
    }

    /**
     * Отправляет индикатор печатания.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param typingIndicator DTO с данными индикатора печатания
     * @return статус успешного выполнения
     */
    @Operation(summary = "Отправка индикатора печатания")
    @PostMapping("/typing")
    public ResponseEntity<Void> sendTypingNotification(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @RequestBody final TypingIndicator typingIndicator) {

        log.info("WEBSOCKET_КОНТРОЛЛЕР_ИНДИКАТОР_ПЕЧАТАНИЯ_НАЧАЛО: " +
                        "отправка индикатора печатания в беседу {} пользователем {}, isTyping: {}",
                typingIndicator.getConversationId(), userId, typingIndicator.isTyping());

        webSocketMessagingService.sendTypingNotification(
                typingIndicator.getConversationId(),
                userId,
                typingIndicator.isTyping()
        );

        log.info("WEBSOCKET_КОНТРОЛЛЕР_ИНДИКАТОР_ПЕЧАТАНИЯ_УСПЕХ: " +
                        "индикатор печатания отправлен в беседу {}",
                typingIndicator.getConversationId());
        return ResponseEntity.ok().build();
    }
}