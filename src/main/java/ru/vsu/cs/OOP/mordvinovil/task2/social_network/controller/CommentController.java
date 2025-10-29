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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.CustomException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.CommentService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового комментария")
    @PostMapping("/create")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentRequest request) {

        User user = userService.getCurrentUser();
        log.info("Пользователь {} создает комментарий к посту {}", user.getId(), request.getPostId());
        CommentResponse response = commentService.create(request, user);
        log.info("Комментарий {} успешно создан пользователем {}", response.getId(), user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование комментария")
    @PutMapping("/edit/{id}")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {

        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} редактирует комментарий {}", currentUser.getId(), id);
        CommentResponse response = commentService.editComment(id, request, currentUser);
        log.info("Комментарий {} успешно отредактирован пользователем {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление комментария")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Boolean> deleteComment(
            @PathVariable Long commentId) {

        try {
            User currentUser = userService.getCurrentUser();
            log.info("Пользователь {} удаляет комментарий {}", currentUser.getId(), commentId);
            CompletableFuture<Boolean> result = commentService.deleteComment(commentId, currentUser);
            Boolean deleteResult = result.get();
            log.info("Комментарий {} успешно удален пользователем {}", commentId, currentUser.getId());
            return ResponseEntity.ok(deleteResult);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Ошибка при удалении комментария {}: {}", commentId, e.getMessage());
            throw new CustomException("Ошибка при удалении комментария: " + e.getMessage());
        }
    }

    @Operation(summary = "Получение комментария по Id")
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable Long commentId) {
        log.info("Запрос на получение комментария {}", commentId);
        CommentResponse response = commentService.getCommentById(commentId);
        log.info("Комментарий {} успешно получен", commentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение комементариев на пост")
    @GetMapping("/onPost/{postId}")
    public ResponseEntity<PageResponse<CommentResponse>> getAllCommentsOnPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "10", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        log.info("Запрос на получение комментариев для поста {}, страница {}, размер {}", postId, pageNumber, size);
        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<CommentResponse> comments = commentService.getAllCommentsOnPost(postId, pageRequest);
        log.info("Получено {} комментариев для поста {}", comments.getContent().size(), postId);
        return ResponseEntity.ok(comments);
    }
}