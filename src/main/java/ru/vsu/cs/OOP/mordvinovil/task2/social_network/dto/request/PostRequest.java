package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Schema(description = "Запрос на создание/редактирование поста")
public class PostRequest {

    @NotBlank
    @Schema(description = "Содержание", example = "Сегодня была отличная тренировка!")
    @Size(max = 500, message = "Длина описания не более 500 символов")
    private String content;

    @Schema(description = "Фото для поста")
    private String imageUrl;
}
