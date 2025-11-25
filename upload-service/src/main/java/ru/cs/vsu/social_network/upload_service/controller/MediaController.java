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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaDeleteRequest;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaDownloadRequest;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaContentResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaMetadataResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.service.AvatarMediaService;
import ru.cs.vsu.social_network.upload_service.service.MediaService;

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
}


