package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.LikeService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

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
    public ResponseEntity<PageResponse<LikePostResponse>> getLikesOnPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {
        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();
        PageResponse<LikePostResponse> pageResponse = likeService.getLikesByPost(postId, pageRequest);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(summary = "Получение списка лайков на комментарии")
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<PageResponse<LikeCommentResponse>> getLikesOnComment(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction) {

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<LikeCommentResponse> pageResponse = likeService.getLikesByComment(commentId, pageRequest);
        return ResponseEntity.ok(pageResponse);
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
