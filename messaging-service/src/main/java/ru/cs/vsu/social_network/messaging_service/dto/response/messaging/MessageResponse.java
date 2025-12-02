package ru.cs.vsu.social_network.messaging_service.dto.response.messaging;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о сообщении")
public class MessageResponse {
    @Schema(description = "ID сообщения")
    private UUID messageId;

    @Schema(description = "Идентификатор отправителя сообщения")
    private UUID senderId;

    @Schema(description = "Идентификатор получателя сообщения")
    private UUID receiverId;

    @Schema(description = "Идентификатор переписки")
    private UUID conversationId;

    @Schema(description = "Содержание сообщения")
    private String content;

    @Schema(description = "Ссылка на изображение сообщения")
    private String imageUrl;

    @Schema(description = "Статус сообщения", example = "SENT")
    private MessageStatus status;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}