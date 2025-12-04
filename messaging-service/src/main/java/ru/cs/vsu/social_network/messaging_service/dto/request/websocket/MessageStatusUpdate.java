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
@Schema(description = "Обновление статуса сообщения для WebSocket")
public class MessageStatusUpdate {

    @Schema(description = "Идентификатор беседы")
    private UUID conversationId;

    @Schema(description = "Количество прочитанных сообщений")
    private int readCount;

    @Schema(description = "Статус сообщения", example = "READ")
    private String status;

    @Schema(description = "Время обновления в миллисекундах")
    private long timestamp;
}