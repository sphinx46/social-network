package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
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

    @Schema(description = "Дата публикации", example = "2000-01-01")
    private LocalDate time;
}