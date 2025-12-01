package ru.cs.vsu.social_network.contents_service.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на удаление изображения с комментария")
public class CommentRemoveImageRequest {
    private static final int MAX_URL_LENGTH = 2048;

    @NotNull(message = "Идентификатор комментария не может быть пустым!")
    @Schema(description = "Идентификатор комментария")
    private UUID commentId;

    @NotBlank(message = "Ссылка на изображение комментария обязательна")
    @Size(max = MAX_URL_LENGTH, message = "Ссылка на изображение комментария слишком длинная")
    @Schema(description = "Ссылка на изображение комментария")
    private String imageUrl;
}