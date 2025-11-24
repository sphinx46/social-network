package ru.cs.vsu.social_network.upload_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о загруженном медиа")
public class MediaResponse {
    @Schema(description = "Идентификатор файла")
    private UUID id;

    @Schema(description = "Идентификатор владельца")
    private UUID ownerId;

    @Schema(description = "Публичная ссылка")
    private String publicUrl;

    @Schema(description = "Уникальное имя объекта в MinIO")
    private String objectName;

    @Schema(description = "Mime-тип файла", example = "image/png")
    private String mimeType;

    @Schema(description = "Размер файла")
    private Long size;

    @Schema(description = "Категория медиа", example = "avatar")
    private String category;

    @Schema(description = "Описание медиа")
    private String description;

    @Schema(description = "Оригинальное имя файла")
    private String originalFileName;
}
