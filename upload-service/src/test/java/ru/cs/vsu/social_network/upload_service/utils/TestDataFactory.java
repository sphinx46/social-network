package ru.cs.vsu.social_network.upload_service.utils;

import org.springframework.mock.web.MockMultipartFile;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaContentResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaMetadataResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Фабрика тестовых данных для повторно используемых объектов upload-service.
 * Содержит компактные методы для создания DTO и сущностей с предсказуемыми значениями.
 * Используется в модульных и интеграционных тестах для обеспечения согласованности тестовых данных.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    /**
     * Создает тестовый запрос на загрузку медиа-файла.
     *
     * @param file загружаемый файл в виде MockMultipartFile
     * @param category категория медиа (например, "avatar", "document")
     * @param description описание медиа-файла
     * @return новый экземпляр MediaUploadRequest с указанными параметрами
     */
    public static MediaUploadRequest createUploadRequest(final MockMultipartFile file,
                                                         final String category,
                                                         final String description) {
        return MediaUploadRequest.builder()
                .file(file)
                .category(category)
                .description(description)
                .build();
    }

    /**
     * Создает тестовый ответ после успешной загрузки медиа-файла.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца медиа
     * @param publicUrl публичный URL для доступа к медиа
     * @param objectName имя объекта в хранилище
     * @param mimeType MIME-тип медиа-файла
     * @param size размер файла в байтах
     * @param category категория медиа
     * @param description описание медиа-файла
     * @param originalFileName оригинальное имя файла
     * @return новый экземпляр MediaResponse с заполненными полями
     */
    public static MediaResponse createMediaResponse(final UUID mediaId,
                                                    final UUID ownerId,
                                                    final String publicUrl,
                                                    final String objectName,
                                                    final String mimeType,
                                                    final long size,
                                                    final String category,
                                                    final String description,
                                                    final String originalFileName) {
        return MediaResponse.builder()
                .id(mediaId)
                .ownerId(ownerId)
                .publicUrl(publicUrl)
                .objectName(objectName)
                .mimeType(mimeType)
                .size(size)
                .category(category)
                .description(description)
                .originalFileName(originalFileName)
                .build();
    }

    /**
     * Создает тестовые метаданные медиа-файла.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца медиа
     * @param category категория медиа
     * @param publicUrl публичный URL для доступа к медиа
     * @return новый экземпляр MediaMetadataResponse с предопределенными тестовыми значениями
     */
    public static MediaMetadataResponse createMetadataResponse(final UUID mediaId,
                                                               final UUID ownerId,
                                                               final String category,
                                                               final String publicUrl) {
        return MediaMetadataResponse.builder()
                .mediaId(mediaId)
                .ownerId(ownerId)
                .category(category)
                .publicUrl(publicUrl)
                .objectName("object-name")
                .bucketName("media")
                .mimeType("image/png")
                .size(42L)
                .originalFileName("sample.png")
                .description("Test file")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовый ответ с контентом медиа-файла для скачивания.
     *
     * @param content входной поток с данными файла
     * @param mimeType MIME-тип медиа-файла
     * @param fileName имя файла для скачивания
     * @param size размер файла в байтах
     * @return новый экземпляр MediaContentResponse с указанными параметрами
     */
    public static MediaContentResponse createContentResponse(final InputStream content,
                                                             final String mimeType,
                                                             final String fileName,
                                                             final long size) {
        return MediaContentResponse.builder()
                .content(content)
                .mimeType(mimeType)
                .originalFileName(fileName)
                .size(size)
                .build();
    }

    /**
     * Создает тестовую сущность медиа-файла.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца медиа
     * @param category категория медиа
     * @param description описание медиа-файла
     * @param objectName имя объекта в хранилище
     * @param originalFileName оригинальное имя файла
     * @param mimeType MIME-тип медиа-файла
     * @param size размер файла в байтах
     * @param bucketName имя бакета в хранилище
     * @param publicUrl публичный URL для доступа к медиа
     * @return новый экземпляр MediaEntity с заполненными полями и временными метками
     */
    public static MediaEntity createMediaEntity(final UUID mediaId,
                                                final UUID ownerId,
                                                final String category,
                                                final String description,
                                                final String objectName,
                                                final String originalFileName,
                                                final String mimeType,
                                                final long size,
                                                final String bucketName,
                                                final String publicUrl) {
        MediaEntity entity = MediaEntity.builder()
                .ownerId(ownerId)
                .category(category)
                .description(description)
                .objectName(objectName)
                .originalFileName(originalFileName)
                .mimeType(mimeType)
                .size(size)
                .bucketName(bucketName)
                .publicUrl(publicUrl)
                .build();
        entity.setId(mediaId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * Создает тестовый файл для загрузки аватара.
     *
     * @param filename имя файла
     * @param contentType MIME-тип файла
     * @param content содержимое файла
     * @return новый экземпляр MockMultipartFile
     */
    public static MockMultipartFile createAvatarFile(final String filename,
                                                     final String contentType,
                                                     final byte[] content) {
        return new MockMultipartFile("file", filename, contentType, content);
    }

    /**
     * Создает тестовый запрос на загрузку аватара.
     *
     * @param file загружаемый файл
     * @param description описание аватара
     * @return новый экземпляр MediaUploadRequest для аватара
     */
    public static MediaUploadRequest createAvatarUploadRequest(final MockMultipartFile file,
                                                               final String description) {
        return MediaUploadRequest.builder()
                .file(file)
                .category("AVATAR")
                .description(description)
                .build();
    }

    /**
     * Создает тестовый ответ для загруженного аватара.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца
     * @return новый экземпляр MediaResponse для аватара
     */
    public static MediaResponse createAvatarResponse(final UUID mediaId, final UUID ownerId) {
        return MediaResponse.builder()
                .id(mediaId)
                .ownerId(ownerId)
                .publicUrl("http://localhost/media/avatar.png")
                .objectName("avatar.png")
                .mimeType("image/png")
                .size(1024L)
                .category("AVATAR")
                .description("Profile avatar")
                .originalFileName("avatar.png")
                .build();
    }

    /**
     * Создает тестовую сущность медиа для аватара.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца
     * @return новый экземпляр MediaEntity для аватара
     */
    public static MediaEntity createAvatarEntity(final UUID mediaId, final UUID ownerId) {
        MediaEntity entity = MediaEntity.builder()
                .ownerId(ownerId)
                .category("AVATAR")
                .description("Profile avatar")
                .objectName("avatar.png")
                .originalFileName("avatar.png")
                .mimeType("image/png")
                .size(1024L)
                .bucketName("media")
                .publicUrl("http://localhost/media/avatar.png")
                .build();
        entity.setId(mediaId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * Создает тестовый файл для загрузки изображения поста.
     *
     * @param filename имя файла
     * @param contentType MIME-тип файла
     * @param content содержимое файла
     * @return новый экземпляр MockMultipartFile
     */
    public static MockMultipartFile createPostImageFile(final String filename,
                                                        final String contentType,
                                                        final byte[] content) {
        return new MockMultipartFile("file", filename, contentType, content);
    }

    /**
     * Создает тестовый запрос на загрузку изображения поста.
     *
     * @param file загружаемый файл
     * @param description описание изображения поста
     * @return новый экземпляр MediaUploadRequest для изображения поста
     */
    public static MediaUploadRequest createPostImageUploadRequest(final MockMultipartFile file,
                                                                  final String description) {
        return MediaUploadRequest.builder()
                .file(file)
                .category("POST_IMAGE")
                .description(description)
                .build();
    }

    /**
     * Создает тестовый ответ для загруженного изображения поста.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца
     * @return новый экземпляр MediaResponse для изображения поста
     */
    public static MediaResponse createPostImageResponse(final UUID mediaId, final UUID ownerId) {
        return MediaResponse.builder()
                .id(mediaId)
                .ownerId(ownerId)
                .publicUrl("http://localhost/media/post-image.jpg")
                .objectName("post-image.jpg")
                .mimeType("image/jpeg")
                .size(2048L)
                .category("POST_IMAGE")
                .description("Post messaging image")
                .originalFileName("post-image.jpg")
                .build();
    }

    /**
     * Создает тестовую сущность медиа для изображения поста.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца
     * @return новый экземпляр MediaEntity для изображения поста
     */
    public static MediaEntity createPostImageEntity(final UUID mediaId, final UUID ownerId) {
        MediaEntity entity = MediaEntity.builder()
                .ownerId(ownerId)
                .category("POST_IMAGE")
                .description("Post messaging image")
                .objectName("post-image.jpg")
                .originalFileName("post-image.jpg")
                .mimeType("image/jpeg")
                .size(2048L)
                .bucketName("media")
                .publicUrl("http://localhost/media/post-image.jpg")
                .build();
        entity.setId(mediaId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * Создает тестовый файл для загрузки изображения комментария.
     *
     * @param filename имя файла
     * @param contentType MIME-тип файла
     * @param content содержимое файла
     * @return новый экземпляр MockMultipartFile
     */
    public static MockMultipartFile createCommentImageFile(final String filename,
                                                           final String contentType,
                                                           final byte[] content) {
        return new MockMultipartFile("file", filename, contentType, content);
    }

    /**
     * Создает тестовый запрос на загрузку изображения комментария.
     *
     * @param file загружаемый файл
     * @param description описание изображения комментария
     * @return новый экземпляр MediaUploadRequest для изображения комментария
     */
    public static MediaUploadRequest createCommentImageUploadRequest(final MockMultipartFile file,
                                                                     final String description) {
        return MediaUploadRequest.builder()
                .file(file)
                .category("COMMENT_IMAGE")
                .description(description)
                .build();
    }

    /**
     * Создает тестовый ответ для загруженного изображения комментария.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца
     * @return новый экземпляр MediaResponse для изображения комментария
     */
    public static MediaResponse createCommentImageResponse(final UUID mediaId,
                                                           final UUID ownerId) {
        return MediaResponse.builder()
                .id(mediaId)
                .ownerId(ownerId)
                .publicUrl("http://localhost/media/comment-image.jpg")
                .objectName("comment-image.jpg")
                .mimeType("image/jpeg")
                .size(1536L)
                .category("COMMENT_IMAGE")
                .description("Comment messaging image")
                .originalFileName("comment-image.jpg")
                .build();
    }

    /**
     * Создает тестовую сущность медиа для изображения комментария.
     *
     * @param mediaId уникальный идентификатор медиа
     * @param ownerId идентификатор владельца
     * @return новый экземпляр MediaEntity для изображения комментария
     */
    public static MediaEntity createCommentImageEntity(final UUID mediaId,
                                                       final UUID ownerId) {
        MediaEntity entity = MediaEntity.builder()
                .ownerId(ownerId)
                .category("COMMENT_IMAGE")
                .description("Comment messaging image")
                .objectName("comment-image.jpg")
                .originalFileName("comment-image.jpg")
                .mimeType("image/jpeg")
                .size(1536L)
                .bucketName("media")
                .publicUrl("http://localhost/media/comment-image.jpg")
                .build();
        entity.setId(mediaId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}