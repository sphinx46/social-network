package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.filters.UserDetailsImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.PostService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.post.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.profile.ProfileNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.security.AccessDeniedException;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService service;
    private final UserService userService;

    @Operation(summary = "Получение списка постов текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<List<PostResponse>> getPostsByCurrentProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

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

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            User currentUser = userService.getByUsername(userDetails.getUsername());
            PostResponse response = service.editPost(request, id, currentUser);
            return ResponseEntity.ok(response);
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Загрузка фото для поста")
    @PostMapping("/{id}/image")
    public ResponseEntity<PostResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile imageFile,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            User currentUser = userService.getByUsername(userDetails.getUsername());
            PostResponse response = service.uploadImage(id, imageFile, currentUser);
            return ResponseEntity.ok(response);
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @Operation(summary = "Удаление изображения поста")
    @DeleteMapping("/{postId}/image")
    public ResponseEntity<PostResponse> removeImage(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            User currentUser = userService.getByUsername(userDetails.getUsername());
            PostResponse response = service.removeImage(postId, currentUser);
            return ResponseEntity.ok(response);
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получение поста по Id")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        try {
            PostResponse response = service.getPostById(postId);
            return ResponseEntity.ok(response);
        } catch (ProfileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @Operation(summary = "Создание нового поста")
    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PostRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            User user = userService.getByUsername(userDetails.getUsername());
            PostResponse response = service.create(user, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}