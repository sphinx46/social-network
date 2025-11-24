package ru.cs.vsu.social_network.upload_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на скачивание медиа-файла")
public class MediaDownloadRequest {

    @Schema(description = "Идентификатор медиа-файла")
    @NotNull(message = "Идентификатор медиа-файла обязателен.")
    private UUID mediaId;
}
