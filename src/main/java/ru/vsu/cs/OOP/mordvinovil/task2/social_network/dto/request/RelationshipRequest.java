package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Schema(description = "Запрос на действие с дружбой")
public class RelationshipRequest {
    @NotNull
    @Schema(description = "Id пользователя, с которым производится действие (отправитель запроса для accept/decline, получатель для block)")
    private Long targetUserId;
}