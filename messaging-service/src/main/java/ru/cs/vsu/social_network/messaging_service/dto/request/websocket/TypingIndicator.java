package ru.cs.vsu.social_network.messaging_service.dto.request.websocket;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Индикатор печатания для WebSocket")
public class TypingIndicator {

    @Schema(description = "Идентификатор беседы")
    private UUID conversationId;

    @Schema(description = "Идентификатор пользователя, который печатает")
    private UUID userId;

    @Schema(description = "Флаг печатания: true - печатает, false - остановился")
    private boolean isTyping;

    @Schema(description = "Время отправки индикатора в миллисекундах")
    private long timestamp;
}