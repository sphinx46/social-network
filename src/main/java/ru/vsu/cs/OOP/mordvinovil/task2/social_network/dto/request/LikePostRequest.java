package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на лайк поста")
public class LikePostRequest {

    @NotNull(message = "ID поста не может быть null.")
    @Schema(description = "ID поста", example = "1", required = true)
    private Long postId;
}