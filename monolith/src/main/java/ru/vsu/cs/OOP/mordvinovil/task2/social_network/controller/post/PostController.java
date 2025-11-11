package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.post;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService service;
    private final UserService userService;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение списка постов текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<PageResponse<PostResponse>> getPostsByCurrentUser(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("ПОСТЫ_ПОЛЬЗОВАТЕЛЯ_ЗАПРОС",
                "Запрос постов текущего пользователя", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<PostResponse> responses = service.getAllPostsByUser(user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", responses.getContent().size());
            successContext.put("totalElements", responses.getTotalElements());

            centralLogger.logInfo("ПОСТЫ_ПОЛЬЗОВАТЕЛЯ_ПОЛУЧЕНЫ",
                    "Посты пользователя успешно получены", successContext);

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            centralLogger.logError("ПОСТЫ_ПОЛЬЗОВАТЕЛЯ_ОШИБКА",
                    "Ошибка при получении постов пользователя", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование поста")
    @PutMapping("/edit/{id}")
    public ResponseEntity<PostResponse> editPost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", id);

        centralLogger.logInfo("ПОСТ_РЕДАКТИРОВАНИЕ_ЗАПРОС",
                "Запрос на редактирование поста", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            PostResponse response = service.editPost(request, id, currentUser);

            centralLogger.logInfo("ПОСТ_ОТРЕДАКТИРОВАН",
                    "Пост успешно отредактирован", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ОШИБКА_РЕДАКТИРОВАНИЯ",
                    "Ошибка при редактировании поста", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Загрузка фото для поста")
    @PostMapping("/{id}/image")
    public ResponseEntity<PostResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile imageFile) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", id);
        context.put("fileName", imageFile.getOriginalFilename());
        context.put("fileSize", imageFile.getSize());

        centralLogger.logInfo("ПОСТ_ИЗОБРАЖЕНИЕ_ЗАГРУЗКА_ЗАПРОС",
                "Запрос на загрузку изображения для поста", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            PostResponse response = service.uploadImage(id, imageFile, currentUser);

            centralLogger.logInfo("ПОСТ_ИЗОБРАЖЕНИЕ_ЗАГРУЖЕНО",
                    "Изображение для поста успешно загружено", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ИЗОБРАЖЕНИЕ_ОШИБКА_ЗАГРУЗКИ",
                    "Ошибка при загрузке изображения для поста", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удаление изображения поста")
    @DeleteMapping("/{postId}/image")
    public ResponseEntity<PostResponse> removeImage(
            @PathVariable Long postId) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);

        centralLogger.logInfo("ПОСТ_ИЗОБРАЖЕНИЕ_УДАЛЕНИЕ_ЗАПРОС",
                "Запрос на удаление изображения поста", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            PostResponse response = service.removeImage(postId, currentUser);

            centralLogger.logInfo("ПОСТ_ИЗОБРАЖЕНИЕ_УДАЛЕНО",
                    "Изображение поста успешно удалено", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ИЗОБРАЖЕНИЕ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении изображения поста", context, e);
            throw e;
        }
    }

    @PreAuthorize("permitAll()")
    @Operation(summary = "Получение поста по Id")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);

        centralLogger.logInfo("ПОСТ_ПОЛУЧЕНИЕ_ЗАПРОС",
                "Запрос на получение поста по ID", context);

        try {
            PostResponse response = service.getPostById(postId);

            centralLogger.logInfo("ПОСТ_ПОЛУЧЕН",
                    "Пост успешно получен", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении поста", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создание нового поста")
    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest request) {
        Map<String, Object> context = new HashMap<>();

        centralLogger.logInfo("ПОСТ_СОЗДАНИЕ_ЗАПРОС",
                "Запрос на создание нового поста", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            PostResponse response = service.create(request, user);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("postId", response.getId());

            centralLogger.logInfo("ПОСТ_СОЗДАН",
                    "Пост успешно создан", successContext);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПОСТ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании поста", context, e);
            throw e;
        }
    }
}