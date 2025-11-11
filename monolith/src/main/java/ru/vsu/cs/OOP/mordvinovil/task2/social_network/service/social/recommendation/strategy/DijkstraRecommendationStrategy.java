package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.recommendation.RecommendationFriendResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.ConnectionStrengthCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DijkstraRecommendationStrategy implements RecommendationStrategy {
    private final ConnectionStrengthCalculator strengthCalculator;
    private final CentralLogger centralLogger;

    /**
     * Получает имя стратегии
     *
     * @return имя стратегии
     */
    @Override
    public String getStrategyName() {
        return "DIJKSTRA";
    }

    /**
     * Генерирует рекомендации друзей
     *
     * @param userId идентификатор пользователя
     * @param socialDistances карта социальных расстояний
     * @return список рекомендаций друзей
     */
    @Override
    public List<RecommendationFriendResponse> generateRecommendations(Long userId, Map<Long, Double> socialDistances) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("socialDistancesCount", socialDistances.size());

        centralLogger.logInfo("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_ГЕНЕРАЦИЯ",
                "Генерация рекомендаций друзей", context);

        try {
            List<RecommendationFriendResponse> recommendations = socialDistances.entrySet().stream()
                    .map(entry -> createRecommendation(userId, entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparing(RecommendationFriendResponse::getScore).reversed())
                    .collect(Collectors.toList());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("recommendationsCount", recommendations.size());

            centralLogger.logInfo("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_СГЕНЕРИРОВАНЫ",
                    "Рекомендации друзей успешно сгенерированы", resultContext);

            return recommendations;
        } catch (Exception e) {
            centralLogger.logError("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_ОШИБКА_ГЕНЕРАЦИИ",
                    "Ошибка при генерации рекомендаций друзей", context, e);
            throw e;
        }
    }

    private RecommendationFriendResponse createRecommendation(Long targetUserId, Long candidateId, Double distance) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", targetUserId);
        context.put("candidateId", candidateId);
        context.put("distance", distance);

        centralLogger.logInfo("РЕКОМЕНДАЦИЯ_СОЗДАНИЕ",
                "Создание рекомендации", context);

        try {
            double distanceScore = 1.0 / (1.0 + distance);
            Map<String, Double> factorScores = strengthCalculator.calculateDetailedStrength(targetUserId, candidateId);

            double finalScore = distanceScore * 0.7 + factorScores.values()
                    .stream().mapToDouble(Double::doubleValue).sum() * 0.3;

            factorScores.put("socialDistance", distanceScore);

            RecommendationFriendResponse response = RecommendationFriendResponse.builder()
                    .recommendedUserId(candidateId)
                    .score(finalScore)
                    .factorScores(factorScores)
                    .createdAt(LocalDateTime.now())
                    .build();

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("finalScore", finalScore);
            resultContext.put("factorScoresCount", factorScores.size());

            centralLogger.logInfo("РЕКОМЕНДАЦИЯ_СОЗДАНА",
                    "Рекомендация успешно создана", resultContext);

            return response;
        } catch (Exception e) {
            centralLogger.logError("РЕКОМЕНДАЦИЯ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании рекомендации", context, e);
            throw e;
        }
    }
}