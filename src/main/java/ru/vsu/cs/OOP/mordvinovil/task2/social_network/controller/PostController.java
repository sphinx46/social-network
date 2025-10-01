package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.filters.UserDetailsImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.PostService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService service;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка постов текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<List<PostResponse>> getPostsByCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getByUsername(userDetails.getUsername());
        List<PostResponse> responses = service.getAllPostsByUser(user);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Редактирование поста")
    @PutMapping("/edit/{id}")
    public ResponseEntity<PostResponse> editPost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User currentUser = userService.getByUsername(userDetails.getUsername());
        PostResponse response = service.editPost(request, id, currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Загрузка фото для поста")
    @PostMapping("/{id}/image")
    public ResponseEntity<PostResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile imageFile,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User currentUser = userService.getByUsername(userDetails.getUsername());
        PostResponse response = service.uploadImage(id, imageFile, currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удаление изображения поста")
    @DeleteMapping("/{postId}/image")
    public ResponseEntity<PostResponse> removeImage(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User currentUser = userService.getByUsername(userDetails.getUsername());
        PostResponse response = service.removeImage(postId, currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение поста по Id")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        PostResponse response = service.getPostById(postId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Создание нового поста")
    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PostRequest request) {

        User user = userService.getByUsername(userDetails.getUsername());
        PostResponse response = service.create(user, request);
        return ResponseEntity.ok(response);
    }
}