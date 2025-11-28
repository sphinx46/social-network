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
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.service.CommentDetailsService;

import java.util.UUID;

/**
 * Контроллер для управления детальной информацией о комментариях.
 * Предоставляет REST API для получения комментариев с связанными сущностями (лайки).
 * Все операции требуют аутентификации через API Gateway.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/comment-details")
public class CommentDetailsController {
    private final CommentDetailsService commentDetailsService;

    @Operation(summary = "Получение детальной информации о комментарии")
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDetailsResponse> getCommentDetails(
            @Valid @PathVariable("commentId") final UUID commentId,
            @RequestParam(defaultValue = "true", required = false) final boolean includeLikes,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer likesLimit) {

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: "
                        + "запрос детальной информации комментария с ID: {}, "
                        + "лайки: {}, лимит лайков: {}",
                commentId, includeLikes, likesLimit);

        final CommentDetailsResponse response = commentDetailsService.getCommentDetails(
                commentId, includeLikes, likesLimit);

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: "
                        + "детальная информация комментария с ID: {} найдена, "
                        + "лайков: {}",
                commentId, response.getLikesCount());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение детальной информации о комментариях поста")
    @GetMapping("/post/{postId}")
    public ResponseEntity<PageResponse<CommentDetailsResponse>> getPostCommentsDetails(
            @Valid @PathVariable("postId") final UUID postId,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction,
            @RequestParam(defaultValue = "true", required = false) final boolean includeLikes,
            @RequestParam(defaultValue = "5", required = false) @Min(1) final Integer likesLimit) {

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОСТА_НАЧАЛО: "
                        + "запрос детальной информации комментариев поста: {}, "
                        + "страница: {}, размер: {}, сортировка: {}, направление: {}, "
                        + "лайки: {}, лимит лайков: {}",
                postId, pageNumber, size, sortedBy, direction,
                includeLikes, likesLimit);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<CommentDetailsResponse> response = commentDetailsService
                .getPostCommentsDetails(postId, pageRequest, includeLikes, likesLimit);

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОСТА_УСПЕХ: "
                        + "найдено {} комментариев с детальной информацией для поста: {}, "
                        + "текущая страница: {}, всего страниц: {}",
                response.getContent().size(), postId,
                response.getCurrentPage(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение детальной информации о комментариях пользователя")
    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<CommentDetailsResponse>> getUserCommentsDetails(
            @Valid @PathVariable("userId") final UUID userId,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction,
            @RequestParam(defaultValue = "true", required = false) final boolean includeLikes,
            @RequestParam(defaultValue = "5", required = false) @Min(1) final Integer likesLimit) {

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: "
                        + "запрос детальной информации комментариев пользователя: {}, "
                        + "страница: {}, размер: {}, сортировка: {}, направление: {}, "
                        + "лайки: {}, лимит лайков: {}",
                userId, pageNumber, size, sortedBy, direction,
                includeLikes, likesLimit);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<CommentDetailsResponse> response = commentDetailsService
                .getUserCommentsDetails(userId, pageRequest, includeLikes, likesLimit);

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: "
                        + "найдено {} комментариев с детальной информацией для пользователя: {}, "
                        + "текущая страница: {}, всего страниц: {}",
                response.getContent().size(), userId,
                response.getCurrentPage(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }
}