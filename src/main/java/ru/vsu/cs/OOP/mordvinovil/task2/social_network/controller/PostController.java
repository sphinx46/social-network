package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
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
    public ResponseEntity<List<PostResponse>> getPostsByCurrentUser() {
        User user = userService.getCurrentUser();
        List<PostResponse> responses = service.getAllPostsByUser(user);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование поста")
    @PutMapping("/edit/{id}")
    public ResponseEntity<PostResponse> editPost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {

        User currentUser = userService.getCurrentUser();
        PostResponse response = service.editPost(request, id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Загрузка фото для поста")
    @PostMapping("/{id}/image")
    public ResponseEntity<PostResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile imageFile) {

        User currentUser = userService.getCurrentUser();
        PostResponse response = service.uploadImage(id, imageFile, currentUser);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление изображения поста")
    @DeleteMapping("/{postId}/image")
    public ResponseEntity<PostResponse> removeImage(
            @PathVariable Long postId) {

        User currentUser = userService.getCurrentUser();
        PostResponse response = service.removeImage(postId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("permitAll()")
    @Operation(summary = "Получение поста по Id")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        PostResponse response = service.getPostById(postId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового поста")
    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest request) {

        User user = userService.getCurrentUser();
        PostResponse response = service.create(request, user);
        return ResponseEntity.ok(response);
    }
}