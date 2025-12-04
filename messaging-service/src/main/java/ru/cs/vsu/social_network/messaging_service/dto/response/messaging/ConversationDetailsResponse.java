package ru.cs.vsu.social_network.messaging_service.dto.response.messaging;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Расширенный ответ с данными о переписке")
public class ConversationDetailsResponse {
    @Schema(description = "ID переписки")
    private UUID conversationId;

    @Schema(description = "Идентификатор первого участника переписки")
    private UUID user1Id;

    @Schema(description = "Идентификатор второго участника переписки")
    private UUID user2Id;

    @Schema(description = "Данные о сообщениях")
    private List<MessageResponse> messages;

    @Schema(description = "Количество сообщений")
    private Long messagesCount;

    @Schema(description = "Идентификатор последнего сообщения")
    private UUID lastMessageId;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}