package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Ответ с данными о лайке")
public class LikeCommentResponse {
    @Schema(description = "ID лайка")
    private Long id;

    @Schema(description = "ID пользователя")
    private Long userId;

    @Schema(description = "Имя пользователя")
    private String username;

    @Schema(description = "ID комментария")
    private Long commentId;

    @Schema(description = "Дата создания")
    private LocalDate createdAt;
}