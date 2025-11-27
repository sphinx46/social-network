package ru.cs.vsu.social_network.contents_service.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
@Schema(description = "Запрос на удаление изображения с комментария")
public class CommentRemoveImageRequest {

    @NotNull(message = "Идентификатор комментария не может быть пустым!")
    @Schema(description = "Идентификатор комментария")
    private UUID commentId;
}