package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.post;

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
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content.PostService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService service;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка постов текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<PageResponse<PostResponse>> getPostsByCurrentUser(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает свои посты, страница {}, размер {}", user.getId(), pageNumber, size);

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        PageResponse<PostResponse> responses = service.getAllPostsByUser(user, pageRequest);
        log.info("Получено {} постов пользователя {}", responses.getContent().size(), user.getId());
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование поста")
    @PutMapping("/edit/{id}")
    public ResponseEntity<PostResponse> editPost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {

        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} редактирует пост {}", currentUser.getId(), id);
        PostResponse response = service.editPost(request, id, currentUser);
        log.info("Пост {} успешно отредактирован пользователем {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Загрузка фото для поста")
    @PostMapping("/{id}/image")
    public ResponseEntity<PostResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile imageFile) {

        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} загружает изображение для поста {}", currentUser.getId(), id);
        PostResponse response = service.uploadImage(id, imageFile, currentUser);
        log.info("Изображение успешно загружено для поста {} пользователем {}", id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление изображения поста")
    @DeleteMapping("/{postId}/image")
    public ResponseEntity<PostResponse> removeImage(
            @PathVariable Long postId) {

        User currentUser = userService.getCurrentUser();
        log.info("Пользователь {} удаляет изображение поста {}", currentUser.getId(), postId);
        PostResponse response = service.removeImage(postId, currentUser);
        log.info("Изображение поста {} успешно удалено пользователем {}", postId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("permitAll()")
    @Operation(summary = "Получение поста по Id")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        log.info("Запрос на получение поста {}", postId);
        PostResponse response = service.getPostById(postId);
        log.info("Пост {} успешно получен", postId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового поста")
    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest request) {

        User user = userService.getCurrentUser();
        log.info("Пользователь {} создает новый пост", user.getId());
        PostResponse response = service.create(request, user);
        log.info("Пост {} успешно создан пользователем {}", response.getId(), user.getId());
        return ResponseEntity.ok(response);
    }
}