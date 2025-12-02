package ru.cs.vsu.social_network.messaging_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageEditRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.messaging_service.service.MessageService;
import ru.cs.vsu.social_network.messaging_service.service.MessagingService;

import java.util.UUID;

/**
 * Контроллер для управления мессенджером.
 * Предоставляет REST API для работы с сообщениями и беседами.
 * Все операции требуют аутентификации через API Gateway.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class MessagingController {

    private final MessagingService messagingService;
    private final MessageService messageService;

    /**
     * Отправляет новое сообщение пользователю.
     *
     * @param senderId идентификатор отправителя из заголовка
     * @param messageCreateRequest DTO с данными для создания сообщения
     * @return отправленное сообщение
     */
    @Operation(summary = "Отправка нового сообщения")
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader("X-User-Id") final UUID senderId,
            @Valid @RequestBody final MessageCreateRequest messageCreateRequest) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ОТПРАВКА_НАЧАЛО: " +
                        "отправка сообщения от {} к {}, длина контента: {}",
                senderId, messageCreateRequest.getReceiverId(),
                messageCreateRequest.getContent().length());

        final MessageResponse response = messagingService.sendMessage(senderId, messageCreateRequest);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ОТПРАВКА_УСПЕХ: " +
                        "сообщение отправлено с ID: {} от {} к {}",
                response.getMessageId(), senderId, messageCreateRequest.getReceiverId());
        return ResponseEntity.ok(response);
    }

    /**
     * Получает переписку между двумя пользователями.
     *
     * @param user1Id идентификатор текущего пользователя из заголовка
     * @param user2Id идентификатор собеседника
     * @param size размер страницы
     * @param pageNumber номер страницы
     * @param sortedBy поле для сортировки
     * @param direction направление сортировки
     * @return страница с перепиской
     */
    @Operation(summary = "Получение переписки между пользователями")
    @GetMapping("/conversation/{user2Id}")
    public ResponseEntity<PageResponse<ConversationDetailsResponse>> getConversationWithUser(
            @RequestHeader("X-User-Id") final UUID user1Id,
            @Valid @PathVariable("user2Id") final UUID user2Id,
            @RequestParam(defaultValue = "20", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПЕРЕПИСКИ_НАЧАЛО: " +
                        "переписка между {} и {}, страница: {}, размер: {}",
                user1Id, user2Id, pageNumber, size);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<ConversationDetailsResponse> response =
                messagingService.getConversationWithUser(user1Id, user2Id, pageRequest);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПЕРЕПИСКИ_УСПЕХ: " +
                        "найдено {} сообщений в переписке между {} и {}",
                response.getContent().isEmpty() ? 0 : response.getContent().get(0).getMessagesCount(),
                user1Id, user2Id);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает список бесед пользователя с предпросмотром сообщений.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param size размер страницы
     * @param pageNumber номер страницы
     * @param sortedBy поле для сортировки
     * @param direction направление сортировки
     * @param previewLimit лимит сообщений для предпросмотра
     * @return страница с беседами и предпросмотром
     */
    @Operation(summary = "Получение бесед пользователя с предпросмотром")
    @GetMapping("/conversations/preview")
    public ResponseEntity<PageResponse<ConversationDetailsResponse>> getUserConversationsWithPreview(
            @RequestHeader("X-User-Id") final UUID userId,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "updatedAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction,
            @RequestParam(defaultValue = "3", required = false) @Min(1) final Integer previewLimit) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_БЕСЕДЫ_ПРЕДПРОСМОТР_НАЧАЛО: " +
                        "беседы пользователя {}, страница: {}, размер: {}, лимит предпросмотра: {}",
                userId, pageNumber, size, previewLimit);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<ConversationDetailsResponse> response =
                messagingService.getUserConversationsWithPreview(userId, pageRequest, previewLimit);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_БЕСЕДЫ_ПРЕДПРОСМОТР_УСПЕХ: " +
                        "найдено {} бесед для пользователя {}",
                response.getContent().size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Отмечает беседу как прочитанную.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param conversationId идентификатор беседы
     * @return количество отмеченных сообщений
     */
    @Operation(summary = "Отметка беседы как прочитанной")
    @PostMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Integer> markConversationAsRead(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @PathVariable("conversationId") final UUID conversationId) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ОТМЕТКА_ПРОЧИТАННОЙ_НАЧАЛО: " +
                "отметка беседы {} как прочитанной пользователем {}", conversationId, userId);

        final int markedCount = messagingService.markConversationAsRead(userId, conversationId);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ОТМЕТКА_ПРОЧИТАННОЙ_УСПЕХ: " +
                "отмечено {} сообщений как прочитанные в беседе {}", markedCount, conversationId);
        return ResponseEntity.ok(markedCount);
    }

    /**
     * Загружает изображение для сообщения.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param request DTO с данными для загрузки изображения
     * @return обновленное сообщение
     */
    @Operation(summary = "Загрузка изображения для сообщения")
    @PostMapping("/message/image/upload")
    public ResponseEntity<MessageResponse> uploadMessageImage(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @RequestBody final MessageUploadImageRequest request) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_НАЧАЛО: " +
                        "загрузка изображения для сообщения {} пользователем {}",
                request.getMessageId(), userId);

        final MessageResponse response = messagingService.uploadMessageImage(userId, request);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_УСПЕХ: " +
                "изображение загружено для сообщения {}", request.getMessageId());
        return ResponseEntity.ok(response);
    }

    /**
     * Удаляет переписку с пользователем.
     *
     * @param userId идентификатор текущего пользователя из заголовка
     * @param otherUserId идентификатор собеседника
     * @return статус успешного выполнения
     */
    @Operation(summary = "Удаление переписки с пользователем")
    @DeleteMapping("/conversation/delete/{otherUserId}")
    public ResponseEntity<Void> deleteConversationWithUser(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @PathVariable("otherUserId") final UUID otherUserId) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_УДАЛЕНИЕ_ПЕРЕПИСКИ_НАЧАЛО: " +
                "удаление переписки между {} и {}", userId, otherUserId);

        messagingService.deleteConversationWithUser(userId, otherUserId);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_УДАЛЕНИЕ_ПЕРЕПИСКИ_УСПЕХ: " +
                "переписка между {} и {} удалена", userId, otherUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получает информацию о чате.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param conversationId идентификатор беседы
     * @return информация о чате
     */
    @Operation(summary = "Получение информации о чате")
    @GetMapping("/chat/{conversationId}")
    public ResponseEntity<ConversationDetailsResponse> getChatInfo(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @PathVariable("conversationId") final UUID conversationId) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ИНФОРМАЦИЯ_ЧАТА_НАЧАЛО: " +
                "информация о чате {} для пользователя {}", conversationId, userId);

        final ConversationDetailsResponse response =
                messagingService.getChatInfo(userId, conversationId);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ИНФОРМАЦИЯ_ЧАТА_УСПЕХ: " +
                "информация о чате {} получена", conversationId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает детальные беседы пользователя.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param size размер страницы
     * @param pageNumber номер страницы
     * @param sortedBy поле для сортировки
     * @param direction направление сортировки
     * @return страница с детальными беседами
     */
    @Operation(summary = "Получение детальных бесед пользователя")
    @GetMapping("/conversations/detailed")
    public ResponseEntity<PageResponse<ConversationDetailsResponse>> getUserConversationsDetailed(
            @RequestHeader("X-User-Id") final UUID userId,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "updatedAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ДЕТАЛЬНЫЕ_БЕСЕДЫ_НАЧАЛО: " +
                        "детальные беседы пользователя {}, страница: {}, размер: {}",
                userId, pageNumber, size);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<ConversationDetailsResponse> response =
                messagingService.getUserConversationsDetailed(userId, pageRequest);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_ДЕТАЛЬНЫЕ_БЕСЕДЫ_УСПЕХ: " +
                        "найдено {} детальных бесед для пользователя {}",
                response.getContent().size(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает количество непрочитанных сообщений в беседе.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param conversationId идентификатор беседы
     * @return количество непрочитанных сообщений
     */
    @Operation(summary = "Получение количества непрочитанных сообщений в беседе")
    @GetMapping("/conversation/{conversationId}/unread")
    public ResponseEntity<Long> getUnreadMessagesCountInConversation(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @PathVariable("conversationId") final UUID conversationId) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_НЕПРОЧИТАННЫЕ_НАЧАЛО: " +
                "количество непрочитанных в беседе {} для пользователя {}", conversationId, userId);

        final Long unreadCount =
                messagingService.getUnreadMessagesCountInConversation(userId, conversationId);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_НЕПРОЧИТАННЫЕ_УСПЕХ: " +
                "в беседе {} найдено {} непрочитанных сообщений", conversationId, unreadCount);
        return ResponseEntity.ok(unreadCount);
    }

    /**
     * Редактирует сообщение.
     *
     * @param userId идентификатор пользователя из заголовка
     * @param request DTO с данными для редактирования сообщения
     * @return отредактированное сообщение
     */
    @Operation(summary = "Редактирование сообщения")
    @PutMapping("/message/edit")
    public ResponseEntity<MessageResponse> editMessage(
            @RequestHeader("X-User-Id") final UUID userId,
            @Valid @RequestBody final MessageEditRequest request) {

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_РЕДАКТИРОВАНИЕ_НАЧАЛО: " +
                "редактирование сообщения {} пользователем {}", request.getMessageId(), userId);

        MessageResponse messageResponse = messageService.editMessage(userId, request);

        log.info("МЕССЕНДЖЕР_КОНТРОЛЛЕР_РЕДАКТИРОВАНИЕ_УСПЕХ: " +
                "сообщение {} отредактировано", request.getMessageId());
        return ResponseEntity.ok(messageResponse);
    }
}