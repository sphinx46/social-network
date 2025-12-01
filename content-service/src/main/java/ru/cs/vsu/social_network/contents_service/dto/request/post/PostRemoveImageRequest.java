package ru.cs.vsu.social_network.contents_service.dto.request.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на удаление изображения с поста")
public class PostRemoveImageRequest {

    @NotNull(message = "Идентификатор поста не может быть пустым!")
    @Schema(description = "Идентификатор поста")
    private UUID postId;
}
