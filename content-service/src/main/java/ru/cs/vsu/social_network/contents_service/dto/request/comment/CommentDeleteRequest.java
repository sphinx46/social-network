package ru.cs.vsu.social_network.contents_service.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на удаление комментария")
public class CommentDeleteRequest {
    @NotNull
    @Schema(description = "Идентификатор поста")
    private UUID postId;

    @NotNull
    @Schema(description = "Идентификатор комментария")
    private UUID commentId;
}