package ru.cs.vsu.social_network.contents_service.dto.response.content;


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
@Schema(description = "Ответ с данными о посте")
public class PostResponse {
    @Schema(description = "ID поста")
    private UUID id;

    @Schema(description = "ID создателя поста")
    private UUID ownerId;

    @Schema(description = "Содержимое поста", example = "Сегодня прекрасный день!")
    private String content;

    @Schema(description = "Публичная ссылка на изображение")
    private String imageUrl;

    @Schema(description = "Дата публикации", example = "2000-01-01")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}