package ru.cs.vsu.social_network.contents_service.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на загрузку изображения для комментария")
public class CommentUploadImageRequest {
    @Schema(description = "Идентификатор комментария")
    @NotNull(message = "ID комментария не может быть пустым")
    private UUID commentId;

    @NotBlank
    @Schema(description = "Фото для поста")
    private String imageUrl;
}