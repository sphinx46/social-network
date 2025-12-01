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
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostEditRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostRemoveImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.service.content.PostService;

import java.util.UUID;

/**
 * Контроллер для управления постами.
 * Предоставляет REST API для создания, редактирования, получения и управления постами.
 * Все операции требуют аутентификации через API Gateway.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    @Operation(summary = "Создание поста")
    @PostMapping("/create")
    public ResponseEntity<PostResponse> create(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final PostCreateRequest postRequest) {

        log.info("ПОСТ_КОНТРОЛЛЕР_СОЗДАНИЕ_НАЧАЛО: "
                        + "создание поста для пользователя: {}, длина контента: {}",
                keycloakUserId, postRequest.getContent().length());

        PostResponse response = postService.create(keycloakUserId, postRequest);

        log.info("ПОСТ_КОНТРОЛЛЕР_СОЗДАНИЕ_УСПЕХ: "
                        + "пост создан с ID: {} для пользователя: {}",
                response.getId(), keycloakUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Редактирование поста")
    @PutMapping("/edit")
    public ResponseEntity<PostResponse> edit(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final PostEditRequest postEditRequest) {

        log.info("ПОСТ_КОНТРОЛЛЕР_РЕДАКТИРОВАНИЕ_НАЧАЛО: "
                        + "редактирование поста с ID: {} пользователем: {}",
                postEditRequest.getPostId(), keycloakUserId);

        PostResponse response = postService.editPost(keycloakUserId, postEditRequest);

        log.info("ПОСТ_КОНТРОЛЛЕР_РЕДАКТИРОВАНИЕ_УСПЕХ: "
                        + "пост с ID: {} отредактирован пользователем: {}",
                response.getId(), keycloakUserId);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Удаление изображения с поста")
    @PatchMapping("/deleteImage")
    public ResponseEntity<PostResponse> deleteImage(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final PostRemoveImageRequest postRemoveImageRequest) {

        log.info("ПОСТ_КОНТРОЛЛЕР_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_НАЧАЛО: "
                        + "удаление изображения с поста с ID: {} пользователем: {}",
                postRemoveImageRequest.getPostId(), keycloakUserId);

        PostResponse response = postService.removeImage(keycloakUserId, postRemoveImageRequest);

        log.info("ПОСТ_КОНТРОЛЛЕР_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_УСПЕХ: "
                        + "изображение удалено с поста с ID: {}",
                response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение поста по Id")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(
            @Valid @PathVariable("postId") final UUID postId) {

        log.info("ПОСТ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: "
                + "запрос поста с ID: {}", postId);

        PostResponse response = postService.getPostById(postId);

        log.info("ПОСТ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: "
                + "пост с ID: {} найден", postId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение постов для определенного пользователя")
    @GetMapping("/pagesPost")
    public ResponseEntity<PageResponse<PostResponse>> getPostsByCurrentUser(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction
    ) {
        log.info("ПОСТ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: "
                        + "запрос постов пользователя: {}, " +
                        "страница: {}, размер: {}, сортировка: {}, направление: {}",
                keycloakUserId, pageNumber, size, sortedBy, direction);

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<PostResponse> response =
                postService.getAllPostsByUser(keycloakUserId, pageRequest);

        log.info("ПОСТ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: "
                        + "найдено {} постов для пользователя: {}, " +
                        "текущая страница: {}, всего страниц: {}",
                response.getContent().size(), keycloakUserId,
                response.getCurrentPage(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }
}