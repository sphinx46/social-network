package ru.cs.vsu.social_network.messaging_service.dto.request.messaging;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на редактирование сообщения")
public class MessageEditRequest {
    @Schema(description = "Идентификатор сообщения")
    @NotNull(message = "ID сообщения не может быть пустым")
    private UUID messageId;

    @Schema(description = "Новое содержание сообщения")
    @NotBlank(message = "Содержание не может быть пустым")
    @Size(max = 2500, message = "Длина сообщения не более 2500 символов")
    private String content;
}