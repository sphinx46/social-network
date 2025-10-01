package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.CommentResponse;
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

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового комментария")
    @PostMapping("/create")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentRequest request) {

        User user = userService.getCurrentUser();
        CommentResponse response = commentService.create(request, user);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование комментария")
    @PutMapping("/edit/{id}")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {

        User currentUser = userService.getCurrentUser();
        CommentResponse response = commentService.editComment(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление комментария")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Boolean> deleteComment(
            @PathVariable Long commentId) {

        try {
            User currentUser = userService.getCurrentUser();
            CompletableFuture<Boolean> result = commentService.deleteComment(commentId, currentUser);
            return ResponseEntity.ok(result.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new CustomException("Ошибка при удалении комментария: " + e.getMessage());
        }
    }

    @Operation(summary = "Получение комментария по Id")
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }
}