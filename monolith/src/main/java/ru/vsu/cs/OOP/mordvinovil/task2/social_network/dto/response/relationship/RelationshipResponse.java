package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.relationship;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о запросе на дружбу")
public class RelationshipResponse {
    @Schema(description = "ID запроса")
    private Long id;

    @Schema(description = "ID пользователя-отправителя")
    private Long senderId;

    @Schema(description = "ID пользователя-получателя")
    private Long receiverId;

    @Schema(description = "Статус запроса")
    private FriendshipStatus status;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}