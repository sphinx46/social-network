package ru.cs.vsu.social_network.contents_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikeCommentRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.service.content.LikeCommentService;

import java.util.UUID;

/**
 * Контроллер для управления лайками комментариев.
 * Предоставляет REST API для создания и удаления лайков на комментарии.
 * Все операции требуют аутентификации через API Gateway.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/like/comment")
public class LikeCommentController {
    private final LikeCommentService likeCommentService;

    @Operation(summary = "Создание лайка на комментарий")
    @PostMapping("/create")
    public ResponseEntity<LikeCommentResponse> create(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final LikeCommentRequest likeCommentRequest) {

        log.info("ЛАЙК_КОММЕНТАРИЙ_КОНТРОЛЛЕР_СОЗДАНИЕ_НАЧАЛО: "
                        + "создание лайка для комментария с ID: {} пользователем: {}",
                likeCommentRequest.getCommentId(), keycloakUserId);

        LikeCommentResponse response =
                likeCommentService.create(keycloakUserId, likeCommentRequest);

        log.info("ЛАЙК_КОММЕНТАРИЙ_КОНТРОЛЛЕР_СОЗДАНИЕ_УСПЕХ: "
                        + "лайк создан с ID: {} для комментария с ID: {} пользователем: {}",
                response.getId(), likeCommentRequest.getCommentId(), keycloakUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удаление лайка с комментария")
    @DeleteMapping("/delete")
    public ResponseEntity<LikeCommentResponse> delete(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final LikeCommentRequest likeCommentRequest) {

        log.info("ЛАЙК_КОММЕНТАРИЙ_КОНТРОЛЛЕР_УДАЛЕНИЕ_НАЧАЛО: "
                        + "удаление лайка с комментария с ID: {} пользователем: {}",
                likeCommentRequest.getCommentId(), keycloakUserId);

        LikeCommentResponse response =
                likeCommentService.delete(keycloakUserId, likeCommentRequest);

        log.info("ЛАЙК_КОММЕНТАРИЙ_КОНТРОЛЛЕР_УДАЛЕНИЕ_УСПЕХ: "
                        + "лайк удален с ID: {} с комментария с ID: {} пользователем: {}",
                response.getId(), likeCommentRequest.getCommentId(), keycloakUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение лайков комментария с пагинацией")
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<PageResponse<LikeCommentResponse>> getLikesByComment(
            @Valid @PathVariable("commentId") final UUID commentId,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction) {

        log.info("ЛАЙК_КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_КОММЕНТАРИЙ_НАЧАЛО: "
                        + "запрос лайков для комментария с ID: {}, страница: {}, размер: {}",
                commentId, pageNumber, size);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<LikeCommentResponse> response =
                likeCommentService.getAllLikesByComment(commentId, pageRequest);

        log.info("ЛАЙК_КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_КОММЕНТАРИЙ_УСПЕХ: "
                        + "найдено {} лайков для комментария с ID: {}, страница: {}",
                response.getContent().size(), commentId, response.getCurrentPage());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение количества лайков комментария")
    @GetMapping("/comment/{commentId}/count")
    public ResponseEntity<Long> getLikesCountByComment(
            @Valid @PathVariable("commentId") final UUID commentId) {

        log.info("ЛАЙК_КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: "
                + "запрос количества лайков для комментария с ID: {}", commentId);

        final Long count = likeCommentService.getLikesCountByComment(commentId);

        log.info("ЛАЙК_КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: "
                + "найдено {} лайков для комментария с ID: {}", count, commentId);
        return ResponseEntity.ok(count);
    }
}