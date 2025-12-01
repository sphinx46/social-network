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
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikePostRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.service.content.LikePostService;

import java.util.UUID;

/**
 * Контроллер для управления лайками постов.
 * Предоставляет REST API для создания и удаления лайков на посты.
 * Все операции требуют аутентификации через API Gateway.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/like/post")
public class LikePostController {
    private final LikePostService likePostService;

    @Operation(summary = "Создание лайка на пост")
    @PostMapping("/create")
    public ResponseEntity<LikePostResponse> create(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final LikePostRequest likePostRequest) {

        log.info("ЛАЙК_ПОСТ_КОНТРОЛЛЕР_СОЗДАНИЕ_НАЧАЛО: "
                        + "создание лайка для поста с ID: {} пользователем: {}",
                likePostRequest.getPostId(), keycloakUserId);

        LikePostResponse response = likePostService.create(keycloakUserId, likePostRequest);

        log.info("ЛАЙК_ПОСТ_КОНТРОЛЛЕР_СОЗДАНИЕ_УСПЕХ: "
                        + "лайк создан с ID: {} для поста с ID: {} пользователем: {}",
                response.getId(), likePostRequest.getPostId(), keycloakUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удаление лайка с поста")
    @DeleteMapping("/delete")
    public ResponseEntity<LikePostResponse> delete(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final LikePostRequest likePostRequest) {

        log.info("ЛАЙК_ПОСТ_КОНТРОЛЛЕР_УДАЛЕНИЕ_НАЧАЛО: "
                        + "удаление лайка с поста с ID: {} пользователем: {}",
                likePostRequest.getPostId(), keycloakUserId);

        LikePostResponse response = likePostService.delete(keycloakUserId, likePostRequest);

        log.info("ЛАЙК_ПОСТ_КОНТРОЛЛЕР_УДАЛЕНИЕ_УСПЕХ: "
                        + "лайк удален с ID: {} с поста с ID: {} пользователем: {}",
                response.getId(), likePostRequest.getPostId(), keycloakUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение лайков поста с пагинацией")
    @GetMapping("/post/{postId}")
    public ResponseEntity<PageResponse<LikePostResponse>> getLikesByPost(
            @Valid @PathVariable("postId") final UUID postId,
            @RequestParam(defaultValue = "10", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction) {

        log.info("ЛАЙК_ПОСТ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОСТ_НАЧАЛО: "
                        + "запрос лайков для поста с ID: {}, страница: {}, размер: {}",
                postId, pageNumber, size);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<LikePostResponse> response =
                likePostService.getAllLikesByPost(postId, pageRequest);

        log.info("ЛАЙК_ПОСТ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОСТ_УСПЕХ: "
                        + "найдено {} лайков для поста с ID: {}, страница: {}",
                response.getContent().size(), postId, response.getCurrentPage());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение количества лайков поста")
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getLikesCountByPost(
            @Valid @PathVariable("postId") final UUID postId) {

        log.info("ЛАЙК_ПОСТ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: "
                + "запрос количества лайков для поста с ID: {}", postId);

        final Long count = likePostService.getLikesCountByPost(postId);

        log.info("ЛАЙК_ПОСТ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: "
                + "найдено {} лайков для поста с ID: {}", count, postId);
        return ResponseEntity.ok(count);
    }
}