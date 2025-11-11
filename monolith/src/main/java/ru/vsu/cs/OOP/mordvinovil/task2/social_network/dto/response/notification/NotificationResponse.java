package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о уведомлении")
public class NotificationResponse {
    @Schema(description = "ID уведомления")
    private Long id;

    @Schema(description = "Тип уведомления")
    private NotificationType type;

    @Schema(description = "Статус уведомления")
    private NotificationStatus status;

    @Schema(description = "Дополнительные данные уведомления")
    private Map<String, Object> additionalData;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}