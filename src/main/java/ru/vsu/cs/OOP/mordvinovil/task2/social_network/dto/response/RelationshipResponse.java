package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;

import java.time.LocalDate;

@Data
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
    private LocalDate createdAt;

    @Schema(description = "Дата обновления")
    private LocalDate updatedAt;
}