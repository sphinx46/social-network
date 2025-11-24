package ru.cs.vsu.social_network.upload_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о медиа с дополнительной информацией")
public class MediaMetadataResponse {
    @Schema(description = "Идентификатор медиа")
    private UUID mediaId;

    @Schema(description = "Идентификатор владельца")
    private UUID ownerId;

    @Schema(description = "Категория медиа", example = "post_image")
    private String category;

    @Schema(description = "Публичная ссылка")
    private String publicUrl;

    @Schema(description = "Имя объекта в MinIO")
    private String objectName;

    @Schema(description = "Название бакета")
    private String bucketName;

    @Schema(description = "Mime-тип файла", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "Размер файла")
    private Long size;

    @Schema(description = "Оригинальное имя файла")
    private String originalFileName;

    @Schema(description = "Описание медиа")
    private String description;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}
