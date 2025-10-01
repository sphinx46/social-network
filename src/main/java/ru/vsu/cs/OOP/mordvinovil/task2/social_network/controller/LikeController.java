package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.LikeService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {
    private final UserService userService;
    private final LikeService likeService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Поставить лайк на пост")
    @PostMapping("/post")
    public ResponseEntity<LikePostResponse> createLikeOnPost(
            @Valid @RequestBody LikePostRequest request) {

        User user = userService.getCurrentUser();
        LikePostResponse response = likeService.likePost(user, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Поставить лайк на комментарий")
    @PostMapping("/comment")
    public ResponseEntity<LikeCommentResponse> createLikeOnComment(
            @Valid @RequestBody LikeCommentRequest request) {

        User user = userService.getCurrentUser();
        LikeCommentResponse response = likeService.likeComment(user, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение списка лайков на посте")
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<LikePostResponse>> getLikesOnPost(
            @PathVariable Long postId) {
        List<LikePostResponse> response = likeService.getLikesByPost(postId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение списка лайков на комментарии")
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<List<LikeCommentResponse>> getLikesOnComment(
            @PathVariable Long commentId) {
        List<LikeCommentResponse> response = likeService.getLikesByComment(commentId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление лайка с поста")
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<LikePostResponse> deleteLikeFromPost(
            @PathVariable Long postId) {
        User currentUser = userService.getCurrentUser();
        LikePostResponse response = likeService.deleteLikeByPost(currentUser, postId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление лайка с комментария")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<LikeCommentResponse> deleteLikeFromComment(
            @PathVariable Long commentId) {
        User currentUser = userService.getCurrentUser();
        LikeCommentResponse response = likeService.deleteLikeByComment(currentUser, commentId);
        return ResponseEntity.ok(response);
    }
}
