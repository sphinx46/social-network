package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDateTime createdAt;
}