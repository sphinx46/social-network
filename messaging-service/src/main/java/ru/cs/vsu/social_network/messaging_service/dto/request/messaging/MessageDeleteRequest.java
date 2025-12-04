package ru.cs.vsu.social_network.messaging_service.dto.request.messaging;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на удаление сообщения")
public class MessageDeleteRequest {
    @Schema(description = "Идентификатор сообщения")
    @NotNull(message = "ID сообщения не может быть пустым")
    private UUID messageId;
}
