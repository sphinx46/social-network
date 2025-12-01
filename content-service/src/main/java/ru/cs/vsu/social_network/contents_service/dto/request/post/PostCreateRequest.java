package ru.cs.vsu.social_network.contents_service.dto.request.post;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Schema(description = "Запрос на создание поста")
public class PostCreateRequest {

    @NotBlank
    @Schema(description = "Содержание", example = "Сегодня была отличная тренировка!")
    @Size(max = 500, message = "Длина описания не более 500 символов")
    private String content;
}