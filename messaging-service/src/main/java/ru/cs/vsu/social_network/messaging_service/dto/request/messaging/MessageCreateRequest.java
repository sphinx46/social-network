package ru.cs.vsu.social_network.messaging_service.dto.request.messaging;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Schema(description = "Запрос на создание сообщения")
public class MessageCreateRequest {
    @NonNull
    @Schema(description = "Идентификатор получателя")
    private UUID receiverId;

    @NotBlank
    @Schema(description = "Содержание", example = "Привет! Как дела?")
    @Size(max = 2500, message = "Длина сообщения не более 2500 символов")
    private String content;
}