package ru.cs.vsu.social_network.contents_service.dto.request.post;


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
@Schema(description = "Запрос на загрузку изображения для поста")
public class PostUploadImageRequest {
    @Schema(description = "Идентификатор поста")
    @NotNull(message = "ID поста не может быть пустым")
    private UUID postId;

    @NotBlank
    @Schema(description = "Фото для поста")
    private String imageUrl;
}
