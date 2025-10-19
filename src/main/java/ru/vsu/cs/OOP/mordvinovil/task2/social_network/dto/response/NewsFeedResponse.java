package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными о ленте новостей")
public class NewsFeedResponse {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "Данные о посте")
    private PostResponse postResponse;

    @Schema(description = "Имя создателя поста", example = "john_doe")
    private String author;
}