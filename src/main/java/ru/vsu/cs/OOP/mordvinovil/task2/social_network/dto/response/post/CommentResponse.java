package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о комментарии")
public class CommentResponse {
    @Schema(description = "ID комментария")
    private Long id;

    @Schema(description = "Имя создателя", example = "john_doe")
    private String username;

    @Schema(description = "Содержимое комментария", example = "Сегодня прекрасный день!")
    private String content;

    @Schema(description = "Фото")
    private String imageUrl;

    @Schema(description = "Данные о лайках на комментарий")
    private List<LikeCommentResponse> likeCommentResponseList;

    @Schema(description = "Дата публикации", example = "2000-01-01")
    private LocalDateTime time;
}