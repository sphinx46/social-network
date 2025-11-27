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
@Schema(description = "Ответ с данными о комментарии")
public class CommentResponse {
    @Schema(description = "ID комментария")
    private UUID id;

    @Schema(description = "ID создателя")
    private UUID ownerId;

    @Schema(description = "ID поста")
    private UUID postId;

    @Schema(description = "Содержимое комментария", example = "Сегодня прекрасный день!")
    private String content;

    @Schema(description = "Фото")
    private String imageUrl;

//    @Schema(description = "Данные о лайках на комментарий")
//    private List<LikeCommentResponse> likeCommentResponseList;

    @Schema(description = "Дата публикации", example = "2000-01-01")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}