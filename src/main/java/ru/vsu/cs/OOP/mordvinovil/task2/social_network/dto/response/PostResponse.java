package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response;

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
@Schema(description = "Ответ с данными о посте")
public class PostResponse {
    @Schema(description = "ID поста")
    private Long id;

    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;

    @Schema(description = "Данные о оставленных под постом комментариев")
    private List<CommentResponse> commentResponseList;

    @Schema(description = "Данные о лайках на пост")
    private List<LikePostResponse> likePostResponseList;

    @Schema(description = "Содержимое поста", example = "Сегодня прекрасный день!")
    private String content;

    @Schema(description = "Фото")
    private String imageUrl;

    @Schema(description = "Дата публикации", example = "2000-01-01")
    private LocalDateTime time;
}