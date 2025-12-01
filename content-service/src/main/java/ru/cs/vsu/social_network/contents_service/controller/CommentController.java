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
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentCreateRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentDeleteRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentEditRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentRemoveImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.service.content.CommentService;

import java.util.UUID;

/**
 * Контроллер для управления комментариями.
 * Предоставляет REST API для создания, редактирования, получения и управления комментариями.
 * Все операции требуют аутентификации через API Gateway.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    /**
     * Создает новый комментарий к посту.
     *
     * @param keycloakUserId идентификатор пользователя из заголовка
     * @param commentCreateRequest DTO с данными для создания комментария
     * @return созданный комментарий
     */
    @Operation(summary = "Создание комментария")
    @PostMapping("/create")
    public ResponseEntity<CommentResponse> create(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final CommentCreateRequest commentCreateRequest) {

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание комментария для пользователя: {}, пост: {}, длина контента: {}",
                keycloakUserId, commentCreateRequest.getPostId(),
                commentCreateRequest.getContent().length());

        final CommentResponse response =
                commentService.createComment(keycloakUserId, commentCreateRequest);

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_СОЗДАНИЕ_УСПЕХ: " +
                        "комментарий создан с ID: {} для пользователя: {}, пост: {}",
                response.getId(), keycloakUserId, commentCreateRequest.getPostId());
        return ResponseEntity.ok(response);
    }

    /**
     * Редактирует существующий комментарий.
     *
     * @param keycloakUserId идентификатор пользователя из заголовка
     * @param commentEditRequest DTO с данными для редактирования комментария
     * @return отредактированный комментарий
     */
    @Operation(summary = "Редактирование комментария")
    @PutMapping("/edit")
    public ResponseEntity<CommentResponse> edit(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final CommentEditRequest commentEditRequest) {

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_РЕДАКТИРОВАНИЕ_НАЧАЛО: " +
                        "редактирование комментария с ID: {} пользователем: {}",
                commentEditRequest.getCommentId(), keycloakUserId);

        final CommentResponse response =
                commentService.editComment(keycloakUserId, commentEditRequest);

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_РЕДАКТИРОВАНИЕ_УСПЕХ: " +
                        "комментарий с ID: {} отредактирован пользователем: {}",
                response.getId(), keycloakUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаляет комментарий.
     *
     * @param keycloakUserId идентификатор пользователя из заголовка
     * @param commentDeleteRequest DTO с данными для удаления комментария
     * @return удаленный комментарий
     */
    @Operation(summary = "Удаление комментария")
    @DeleteMapping("/delete")
    public ResponseEntity<CommentResponse> delete(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final CommentDeleteRequest commentDeleteRequest) {

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_УДАЛЕНИЕ_НАЧАЛО: " +
                        "удаление комментария с ID: {} пользователем: {}",
                commentDeleteRequest.getCommentId(), keycloakUserId);

        final CommentResponse response =
                commentService.deleteComment(keycloakUserId, commentDeleteRequest);

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_УДАЛЕНИЕ_УСПЕХ: " +
                        "комментарий с ID: {} удален пользователем: {}",
                response.getId(), keycloakUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаляет изображение с комментария.
     *
     * @param keycloakUserId идентификатор пользователя из заголовка
     * @param commentRemoveImageRequest DTO с данными для удаления изображения
     * @return комментарий с удаленным изображением
     */
    @Operation(summary = "Удаление изображения с комментария")
    @PatchMapping("/deleteImage")
    public ResponseEntity<CommentResponse> deleteImage(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @Valid @RequestBody final CommentRemoveImageRequest commentRemoveImageRequest) {

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_НАЧАЛО: " +
                        "удаление изображения с комментария с ID: {} пользователем: {}",
                commentRemoveImageRequest.getCommentId(), keycloakUserId);

        final CommentResponse response =
                commentService.removeImage(keycloakUserId, commentRemoveImageRequest);

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_УСПЕХ: " +
                        "изображение удалено с комментария с ID: {}",
                response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Получает комментарий по его идентификатору.
     *
     * @param commentId идентификатор комментария
     * @return найденный комментарий
     */
    @Operation(summary = "Получение комментария по Id")
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @Valid @PathVariable("commentId") final UUID commentId) {

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: " +
                "запрос комментария с ID: {}", commentId);

        final CommentResponse response = commentService.getCommentById(commentId);

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: " +
                "комментарий с ID: {} найден", commentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает комментарии пользователя к определенному посту.
     *
     * @param keycloakUserId идентификатор пользователя из заголовка
     * @param postId идентификатор поста
     * @param size размер страницы
     * @param pageNumber номер страницы
     * @param sortedBy поле для сортировки
     * @param direction направление сортировки
     * @return страница с комментариями пользователя к посту
     */
    @Operation(summary = "Получение комментариев пользователя к посту")
    @GetMapping("/pagesCommentByUserAndPost")
    public ResponseEntity<PageResponse<CommentResponse>> getCommentsByPostAndOwner(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @RequestParam final UUID postId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction
    ) {
        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ПОСТУ_И_ВЛАДЕЛЬЦУ_НАЧАЛО: " +
                        "запрос комментариев пользователя: {} для поста: {}, " +
                        "страница: {}, размер: {}, сортировка: {}, направление: {}",
                keycloakUserId, postId, pageNumber, size, sortedBy, direction);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<CommentResponse> response =
                commentService.getCommentsByPostAndOwner(keycloakUserId, postId, pageRequest);

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ПОСТУ_И_ВЛАДЕЛЬЦУ_УСПЕХ: " +
                        "найдено {} комментариев пользователя: {} для поста: {}, " +
                        "текущая страница: {}, всего страниц: {}",
                response.getContent().size(), keycloakUserId, postId,
                response.getCurrentPage(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }

    /**
     * Получает все комментарии к определенному посту.
     *
     * @param postId идентификатор поста
     * @param size размер страницы
     * @param pageNumber номер страницы
     * @param sortedBy поле для сортировки
     * @param direction направление сортировки
     * @return страница со всеми комментариями к посту
     */
    @Operation(summary = "Получение всех комментариев к посту")
    @GetMapping("/pagesCommentByPost")
    public ResponseEntity<PageResponse<CommentResponse>> getCommentsByPost(
            @RequestParam final UUID postId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) final Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) final String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) final String direction
    ) {
        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ПОСТУ_НАЧАЛО: " +
                        "запрос всех комментариев для поста: {}, " +
                        "страница: {}, размер: {}, сортировка: {}, направление: {}",
                postId, pageNumber, size, sortedBy, direction);

        final var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        final PageResponse<CommentResponse> response =
                commentService.getCommentsByPost(postId, pageRequest);

        log.info("КОММЕНТАРИЙ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ПОСТУ_УСПЕХ: " +
                        "найдено {} комментариев для поста: {}, " +
                        "текущая страница: {}, всего страниц: {}",
                response.getContent().size(), postId,
                response.getCurrentPage(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }
}

