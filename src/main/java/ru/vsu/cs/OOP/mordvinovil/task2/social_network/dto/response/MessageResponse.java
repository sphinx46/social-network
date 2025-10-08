package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;

import java.time.LocalDate;

@Builder
@Data
@Schema(description = "Ответ с данными о сообщении")
public class MessageResponse {
    @Schema(description = "ID сообщения")
    private Long id;

    @Schema(description = "Имя отправителя", example = "john_doe")
    private String senderUsername;

    @Schema(description = "Имя получателя", example = "john_doe2")
    private String receiverUsername;

    @Schema(description = "Содержимое сообщения", example = "Сегодня прекрасный день!")
    private String content;

    @Schema(description = "Фото")
    private String imageUrl;

    @Schema(description = "Статус сообщения", example = "SENT")
    private MessageStatus status;

    @Schema(description = "Дата отправления", example = "2000-01-01")
    private LocalDate createdAt;

    @Schema(description = "Дата обновления", example = "2000-01-02")
    private LocalDate updatedAt;
}