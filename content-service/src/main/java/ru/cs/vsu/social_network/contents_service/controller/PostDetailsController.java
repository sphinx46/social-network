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
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.service.PostDetailsService;

import java.util.UUID;

/**
 * Контроллер для управления детальной информацией о постах.
 * Предоставляет REST API для получения постов с связанными сущностями (лайки, комментарии).
 * Все операции требуют аутентификации через API Gateway.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/post-details")
public class PostDetailsController {
    private final PostDetailsService postDetailsService;

    @Operation(summary = "Получение детальной информации о посте")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailsResponse> getPostDetails(
            @Valid @PathVariable("postId") final UUID postId,
            @RequestParam(defaultValue = "true", required = false) final boolean includeComments,
            @RequestParam(defaultValue = "true", required = false) final boolean includeLikes,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer commentsLimit,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer likesLimit) {

        log.info("ПОСТ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: "
                        + "запрос детальной информации поста с ID: {}, "
                        + "комментарии: {}, лайки: {}, лимит комментариев: {}, лимит лайков: {}",
                postId, includeComments, includeLikes, commentsLimit, likesLimit);

        final PostDetailsResponse response = postDetailsService.getPostDetails(
                postId, includeComments, includeLikes, commentsLimit, likesLimit);

        log.info("ПОСТ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: "
                        + "детальная информация поста с ID: {} найдена, "
                        + "комментариев: {}, лайков: {}",
                postId, response.getCommentsCount(), response.getLikesCount());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение детальной информации о постах пользователя")
    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<PostDetailsResponse>> getUserPostsDetails(
            @Valid @PathVariable("userId") final UUID userId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction,
            @RequestParam(defaultValue = "true", required = false) final boolean includeComments,
            @RequestParam(defaultValue = "true", required = false) final boolean includeLikes,
            @RequestParam(defaultValue = "5", required = false) @Min(1) final Integer commentsLimit,
            @RequestParam(defaultValue = "5", required = false) @Min(1) final Integer likesLimit) {

        log.info("ПОСТ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: "
                        + "запрос детальной информации постов пользователя: {}, "
                        + "страница: {}, размер: {}, сортировка: {}, направление: {}, "
                        + "комментарии: {}, лайки: {}, лимит комментариев: {}, лимит лайков: {}",
                userId, pageNumber, size, sortedBy, direction,
                includeComments, includeLikes, commentsLimit, likesLimit);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<PostDetailsResponse> response = postDetailsService
                .getUserPostsDetails(userId, pageRequest, includeComments,
                        includeLikes, commentsLimit, likesLimit);

        log.info("ПОСТ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: "
                        + "найдено {} постов с детальной информацией для пользователя: {}, "
                        + "текущая страница: {}, всего страниц: {}",
                response.getContent().size(), userId,
                response.getCurrentPage(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение детальной информации о всех постах с пагинацией")
    @GetMapping("/all")
    public ResponseEntity<PageResponse<PostDetailsResponse>> getAllPostsDetails(
            @RequestParam(defaultValue = "1", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction,
            @RequestParam(defaultValue = "true", required = false) final boolean includeComments,
            @RequestParam(defaultValue = "true", required = false) final boolean includeLikes,
            @RequestParam(defaultValue = "3", required = false) @Min(1) final Integer commentsLimit,
            @RequestParam(defaultValue = "3", required = false) @Min(1) final Integer likesLimit) {

        log.info("ПОСТ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ВСЕХ_НАЧАЛО: "
                        + "запрос детальной информации всех постов, "
                        + "страница: {}, размер: {}, сортировка: {}, направление: {}, "
                        + "комментарии: {}, лайки: {}, лимит комментариев: {}, лимит лайков: {}",
                pageNumber, size, sortedBy, direction,
                includeComments, includeLikes, commentsLimit, likesLimit);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<PostDetailsResponse> response = postDetailsService
                .getAllPostsDetails(pageRequest, includeComments,
                        includeLikes, commentsLimit, likesLimit);

        log.info("ПОСТ_ДЕТАЛИ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ВСЕХ_УСПЕХ: "
                        + "найдено {} постов с детальной информацией, "
                        + "текущая страница: {}, всего страниц: {}",
                response.getContent().size(),
                response.getCurrentPage(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }
}