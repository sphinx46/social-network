package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(description = "Запрос на создание/редактирование сообщения")
public class MessageRequest {
    @NotNull(message = "ID не может быть null.")
    @Schema(description = "ID получателя", example = "1")
    private Long receiverUserId;

    @NotBlank
    @Schema(description = "Содержание", example = "Привет! Как дела?")
    private String content;

    @Schema(description = "Фото", example = "image.jpg")
    private String imageUrl;
}
