package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.post;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content.LikeService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {
    private final UserService userService;
    private final LikeService likeService;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Поставить лайк на пост")
    @PostMapping("/post")
    public ResponseEntity<LikePostResponse> createLikeOnPost(
            @Valid @RequestBody LikePostRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", request.getPostId());

        centralLogger.logInfo("ЛАЙК_ПОСТ_СОЗДАНИЕ_ЗАПРОС",
                "Запрос на создание лайка для поста", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            LikePostResponse response = likeService.likePost(user, request);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("likeId", response.getId());

            centralLogger.logInfo("ЛАЙК_ПОСТ_СОЗДАН",
                    "Лайк для поста успешно создан", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ЛАЙК_ПОСТ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании лайка для поста", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Поставить лайк на комментарий")
    @PostMapping("/comment")
    public ResponseEntity<LikeCommentResponse> createLikeOnComment(
            @Valid @RequestBody LikeCommentRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("commentId", request.getCommentId());

        centralLogger.logInfo("ЛАЙК_КОММЕНТАРИЙ_СОЗДАНИЕ_ЗАПРОС",
                "Запрос на создание лайка для комментария", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            LikeCommentResponse response = likeService.likeComment(user, request);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("likeId", response.getId());

            centralLogger.logInfo("ЛАЙК_КОММЕНТАРИЙ_СОЗДАН",
                    "Лайк для комментария успешно создан", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ЛАЙК_КОММЕНТАРИЙ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании лайка для комментария", context, e);
            throw e;
        }
    }

    @Operation(summary = "Получение списка лайков на посте")
    @GetMapping("/post/{postId}")
    public ResponseEntity<PageResponse<LikePostResponse>> getLikesOnPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ЛАЙКИ_ПОСТА_ЗАПРОС",
                "Запрос лайков для поста", context);

        try {
            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<LikePostResponse> pageResponse = likeService.getLikesByPost(postId, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", pageResponse.getContent().size());
            successContext.put("totalElements", pageResponse.getTotalElements());

            centralLogger.logInfo("ЛАЙКИ_ПОСТА_ПОЛУЧЕНЫ",
                    "Лайки для поста успешно получены", successContext);

            return ResponseEntity.ok(pageResponse);
        } catch (Exception e) {
            centralLogger.logError("ЛАЙКИ_ПОСТА_ОШИБКА",
                    "Ошибка при получении лайков для поста", context, e);
            throw e;
        }
    }

    @Operation(summary = "Получение списка лайков на комментарии")
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<PageResponse<LikeCommentResponse>> getLikesOnComment(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {
        Map<String, Object> context = new HashMap<>();
        context.put("commentId", commentId);
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ЛАЙКИ_КОММЕНТАРИЯ_ЗАПРОС",
                "Запрос лайков для комментария", context);

        try {
            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<LikeCommentResponse> pageResponse = likeService.getLikesByComment(commentId, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", pageResponse.getContent().size());
            successContext.put("totalElements", pageResponse.getTotalElements());

            centralLogger.logInfo("ЛАЙКИ_КОММЕНТАРИЯ_ПОЛУЧЕНЫ",
                    "Лайки для комментария успешно получены", successContext);

            return ResponseEntity.ok(pageResponse);
        } catch (Exception e) {
            centralLogger.logError("ЛАЙКИ_КОММЕНТАРИЯ_ОШИБКА",
                    "Ошибка при получении лайков для комментария", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление лайка с поста")
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<LikePostResponse> deleteLikeFromPost(
            @PathVariable Long postId) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);

        centralLogger.logInfo("ЛАЙК_ПОСТ_УДАЛЕНИЕ_ЗАПРОС",
                "Запрос на удаление лайка с поста", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            LikePostResponse response = likeService.deleteLikeByPost(currentUser, postId);

            centralLogger.logInfo("ЛАЙК_ПОСТ_УДАЛЕН",
                    "Лайк с поста успешно удален", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ЛАЙК_ПОСТ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении лайка с поста", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление лайка с комментария")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<LikeCommentResponse> deleteLikeFromComment(
            @PathVariable Long commentId) {
        Map<String, Object> context = new HashMap<>();
        context.put("commentId", commentId);

        centralLogger.logInfo("ЛАЙК_КОММЕНТАРИЙ_УДАЛЕНИЕ_ЗАПРОС",
                "Запрос на удаление лайка с комментария", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            LikeCommentResponse response = likeService.deleteLikeByComment(currentUser, commentId);

            centralLogger.logInfo("ЛАЙК_КОММЕНТАРИЙ_УДАЛЕН",
                    "Лайк с комментария успешно удален", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ЛАЙК_КОММЕНТАРИЙ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении лайка с комментария", context, e);
            throw e;
        }
    }
}