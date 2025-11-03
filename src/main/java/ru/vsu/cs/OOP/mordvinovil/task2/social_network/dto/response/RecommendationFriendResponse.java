package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с рекомендацией в друзья")
public class RecommendationFriendResponse {
    @Schema(description = "Id рекоммендуемого пользователя")
    private Long recommendedUserId;

    @Schema(description = "Очки рекомендации")
    private Double score;

    @Schema(description = "Факторы, повлиявшие на рекомендации")
    private Map<String, Double> factorScores;

    @Schema(description = "Время генерации рекомендации")
    private LocalDateTime createdAt;
}
