package ru.cs.vsu.social_network.contents_service.dto.request.like;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Schema(description = "Запрос на создание/удаление лайка на комментарий")
public class LikeCommentRequest {

    @NotNull(message = "ID комментария не может быть null.")
    @Schema(description = "ID поста", example = "1")
    private UUID commentId;
}
