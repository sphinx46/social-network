package ru.cs.vsu.social_network.upload_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Value
@Builder
@Schema(description = "Ответ со скачиваемым содержимым медиа")
public class MediaContentResponse {
    @Schema(description = "Поток с бинарными данными")
    InputStream content;

    @Schema(description = "Mime-тип файла")
    String mimeType;

    @Schema(description = "Оригинальное имя файла")
    String originalFileName;

    @Schema(description = "Размер контента в байтах")
    long size;
}

