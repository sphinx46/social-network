package ru.cs.vsu.social_network.upload_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaDeleteRequest;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaDownloadRequest;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaContentResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaMetadataResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.service.*;

import java.util.UUID;

/**
 * Контроллер для работы с медиа-файлами.
 * Предоставляет API для загрузки, скачивания и управления медиа-контентом.
 * Поддерживает различные типы медиа: аватары, изображения постов и другие.
 *
 * @author REST API
 * @version 1.0
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
public class MediaController {

    private final MediaService mediaService;
    private final AvatarMediaService avatarMediaService;
    private final PostImageMediaService postImageMediaService;
    private final CommentImageMediaService commentImageMediaService;
    private final MessageImageMediaService messageImageMediaService;

    /**
     * Загружает медиа-файл общего назначения.
     * Используется для загрузки медиа, не требующих специальной обработки.
     *
     * @param request параметры загрузки файла
     * @return данные сохранённого медиа
     */
    @Operation(summary = "Загрузка медиа-файла")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadFile(
            @Valid @ModelAttribute final MediaUploadRequest request) {
        log.info("МЕДИА_CONTROLLER_ЗАГРУЗКА_ОБЩАЯ: категория={}", request.getCategory());
        final MediaResponse response = mediaService.uploadFile(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Загружает аватар пользователя.
     * Включает специфичную валидацию и отправку событий для обновления профиля.
     *
     * @param request параметры загрузки аватара
     * @return данные сохранённого аватара
     */
    @Operation(summary = "Загрузка аватара пользователя")
    @PostMapping(value = "/avatars", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadAvatar(
            @Valid @ModelAttribute final MediaUploadRequest request) {
        log.info("МЕДИА_CONTROLLER_ЗАГРУЗКА_АВАТАРА: пользовательский запрос");
        final MediaResponse response = avatarMediaService.uploadAvatar(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Загружает изображение поста.
     * Включает специфичную валидацию и отправку событий для обновления контента.
     *
     * @param request параметры загрузки изображения поста
     * @param postId идентификатор поста
     * @return данные сохранённого изображения поста
     */
    @Operation(summary = "Загрузка изображения поста")
    @PostMapping(value = "/post-images/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadPostImage(
            @Valid @ModelAttribute final MediaUploadRequest request,
            @PathVariable final UUID postId) {
        log.info("МЕДИА_CONTROLLER_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_ПОСТА: postId={}", postId);
        final MediaResponse response = postImageMediaService.uploadPostImage(request, postId);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Загружает изображение сообщения.
     * Включает специфичную валидацию и отправку событий для обновления контента.
     *
     * @param request параметры загрузки изображения сообщения
     * @param messageId идентификатор сообщения
     * @return данные сохранённого изображения сообщения
     */
    @Operation(summary = "Загрузка изображения сообщения")
    @PostMapping(value = "/message-images/{messageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadMessageImage(
            @Valid @ModelAttribute final MediaUploadRequest request,
            @PathVariable final UUID messageId) {
        log.info("МЕДИА_CONTROLLER_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ: messageId={}", messageId);
        final MediaResponse response = messageImageMediaService.uploadMessageImage(request, messageId);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Возвращает метаданные медиа.
     *
     * @param mediaId идентификатор медиа
     * @return подробные метаданные файла
     */
    @Operation(summary = "Получение метаданных медиа")
    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaMetadataResponse> getMetadata(
            @PathVariable final UUID mediaId) {
        log.debug("МЕДИА_CONTROLLER_МЕТАДАННЫЕ: mediaId={}", mediaId);
        return ResponseEntity.ok(mediaService.getMetaData(mediaId));
    }

    /**
     * Скачивает файл по идентификатору.
     *
     * @param mediaId идентификатор медиа
     * @return поток файла с соответствующими HTTP-заголовками
     */
    @Operation(summary = "Скачать медиа-файл")
    @GetMapping("/{mediaId}/content")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable final UUID mediaId) {
        log.debug("МЕДИА_CONTROLLER_СКАЧИВАНИЕ: mediaId={}", mediaId);

        final MediaDownloadRequest request = MediaDownloadRequest.builder()
                .mediaId(mediaId)
                .build();
        final MediaContentResponse content = mediaService.download(request);

        final String filename = content.getOriginalFileName() != null
                ? content.getOriginalFileName()
                : mediaId + ".bin";
        final InputStreamResource resource = new InputStreamResource(content.getContent());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentLength(content.getSize())
                .contentType(MediaType.parseMediaType(content.getMimeType()))
                .body(resource);
    }

    /**
     * Удаляет медиа-файл.
     *
     * @param mediaId идентификатор медиа
     * @return пустой ответ со статусом 204 No Content
     */
    @Operation(summary = "Удаление медиа-файла")
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> delete(
            @PathVariable final UUID mediaId) {
        log.info("МЕДИА_CONTROLLER_УДАЛЕНИЕ: mediaId={}", mediaId);

        mediaService.deleteFile(MediaDeleteRequest.builder()
                .mediaId(mediaId)
                .build());

        return ResponseEntity.noContent().build();
    }

    /**
     * Загружает изображение комментария.
     * Включает специфичную валидацию и отправку событий для обновления контента.
     *
     * @param request параметры загрузки изображения комментария
     * @param commentId идентификатор комментария
     * @param postId идентификатор поста
     * @return данные сохранённого изображения комментария
     */
    @Operation(summary = "Загрузка изображения комментария")
    @PostMapping(value = "/comment-images/{commentId}/post/{postId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadCommentImage(
            @Valid @ModelAttribute final MediaUploadRequest request,
            @PathVariable final UUID commentId,
            @PathVariable final UUID postId) {

        log.info("МЕДИА_CONTROLLER_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ: " +
                "commentId={}, postId={}", commentId, postId);

        final MediaResponse response = commentImageMediaService.
                uploadCommentImage(request, commentId, postId);
        return ResponseEntity.status(201).body(response);
    }
}