package ru.cs.vsu.social_network.contents_service.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
@Schema(description = "Запрос на редактирование комментария")
public class CommentEditRequest {
    @Schema(description = "Идентификатор комментария")
    @NotNull(message = "ID комментария не может быть пустым")
    private UUID commentId;

    @Schema(description = "Новое содержание комментария")
    @NotBlank(message = "Содержание не может быть пустым")
    @Size(max = 500, message = "Длина описания не более 500 символов")
    private String content;
}