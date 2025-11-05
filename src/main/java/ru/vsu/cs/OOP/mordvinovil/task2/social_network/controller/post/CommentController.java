package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.post;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.CustomException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content.CommentService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового комментария")
    @PostMapping("/create")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", request.getPostId());

        centralLogger.logInfo("КОММЕНТАРИЙ_СОЗДАНИЕ_ЗАПРОС",
                "Запрос на создание комментария", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            CommentResponse response = commentService.create(request, user);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("commentId", response.getId());

            centralLogger.logInfo("КОММЕНТАРИЙ_СОЗДАН",
                    "Комментарий успешно создан", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("КОММЕНТАРИЙ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании комментария", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование комментария")
    @PutMapping("/edit/{id}")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("commentId", id);

        centralLogger.logInfo("КОММЕНТАРИЙ_РЕДАКТИРОВАНИЕ_ЗАПРОС",
                "Запрос на редактирование комментария", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            CommentResponse response = commentService.editComment(id, request, currentUser);

            centralLogger.logInfo("КОММЕНТАРИЙ_ОТРЕДАКТИРОВАН",
                    "Комментарий успешно отредактирован", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("КОММЕНТАРИЙ_ОШИБКА_РЕДАКТИРОВАНИЯ",
                    "Ошибка при редактировании комментария", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление комментария")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Boolean> deleteComment(
            @PathVariable Long commentId) {
        Map<String, Object> context = new HashMap<>();
        context.put("commentId", commentId);

        centralLogger.logInfo("КОММЕНТАРИЙ_УДАЛЕНИЕ_ЗАПРОС",
                "Запрос на удаление комментария", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            CompletableFuture<Boolean> result = commentService.deleteComment(commentId, currentUser);
            Boolean deleteResult = result.get();

            centralLogger.logInfo("КОММЕНТАРИЙ_УДАЛЕН",
                    "Комментарий успешно удален", context);

            return ResponseEntity.ok(deleteResult);
        } catch (InterruptedException | ExecutionException e) {
            centralLogger.logError("КОММЕНТАРИЙ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении комментария", context, e);
            throw new CustomException("Ошибка при удалении комментария: " + e.getMessage());
        }
    }

    @Operation(summary = "Получение комментария по Id")
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable Long commentId) {
        Map<String, Object> context = new HashMap<>();
        context.put("commentId", commentId);

        centralLogger.logInfo("КОММЕНТАРИЙ_ПОЛУЧЕНИЕ_ЗАПРОС",
                "Запрос на получение комментария по ID", context);

        try {
            CommentResponse response = commentService.getCommentById(commentId);

            centralLogger.logInfo("КОММЕНТАРИЙ_ПОЛУЧЕН",
                    "Комментарий успешно получен", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("КОММЕНТАРИЙ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении комментария", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("КОММЕНТАРИИ_ПОСТА_ЗАПРОС",
                "Запрос комментариев для поста", context);

        try {
            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<CommentResponse> comments = commentService.getAllCommentsOnPost(postId, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", comments.getContent().size());
            successContext.put("totalElements", comments.getTotalElements());

            centralLogger.logInfo("КОММЕНТАРИИ_ПОСТА_ПОЛУЧЕНЫ",
                    "Комментарии для поста успешно получены", successContext);

            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            centralLogger.logError("КОММЕНТАРИИ_ПОСТА_ОШИБКА",
                    "Ошибка при получении комментариев для поста", context, e);
            throw e;
        }
    }
}