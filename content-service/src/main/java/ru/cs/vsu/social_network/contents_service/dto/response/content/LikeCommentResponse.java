package ru.cs.vsu.social_network.contents_service.dto.response.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о лайке")
public class LikeCommentResponse {
    @Schema(description = "ID лайка")
    private UUID id;

    @Schema(description = "ID владельца лайка")
    private UUID ownerId;

    @Schema(description = "ID комментария")
    private UUID commentId;

    @Schema(description = "Дата создания")
    private LocalDateTime createdAt;
}