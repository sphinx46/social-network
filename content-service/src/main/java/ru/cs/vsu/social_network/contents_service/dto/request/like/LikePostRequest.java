package ru.cs.vsu.social_network.contents_service.dto.request.like;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Запрос создание/удаление лайка на поста")
public class LikePostRequest {

    @NotNull(message = "ID поста не может быть null.")
    @Schema(description = "ID поста", example = "1")
    private UUID postId;
}