package ru.cs.vsu.social_network.contents_service.dto.response.content;

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
@Schema(description = "Расширенный ответ с данными о посте")
public class PostDetailsResponse {
    @Schema(description = "ID поста")
    private UUID id;

    @Schema(description = "ID создателя поста")
    private UUID ownerId;

    @Schema(description = "Содержимое поста")
    private String content;

    @Schema(description = "Публичная ссылка на изображение")
    private String imageUrl;

    @Schema(description = "Данные о комментариях")
    private List<CommentResponse> comments;

    @Schema(description = "Данные о лайках")
    private List<LikePostResponse> likes;

    @Schema(description = "Количество лайков")
    private Long likesCount;

    @Schema(description = "Количество комментариев")
    private Long commentsCount;

    @Schema(description = "Дата публикации")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления")
    private LocalDateTime updatedAt;
}