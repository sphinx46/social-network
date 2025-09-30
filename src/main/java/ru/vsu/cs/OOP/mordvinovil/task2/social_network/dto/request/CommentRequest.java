package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание/редактирование комментария")
public class CommentRequest {

    @NotNull
    @Schema(description = "Id Поста, который будет содержать комментарий")
    private Long postId;

    @NotBlank
    @Schema(description = "Содержание", example = "Спасибо за контент!")
    @Size(max = 500, message = "Длина описания не более 500 символов")
    private String content;

    @Schema(description = "Фото для комментария")
    private String imageUrl;
}