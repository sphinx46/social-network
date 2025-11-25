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
}
