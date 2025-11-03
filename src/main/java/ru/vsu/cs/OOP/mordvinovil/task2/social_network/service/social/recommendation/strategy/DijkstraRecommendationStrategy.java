package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RecommendationFriendResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.ConnectionStrengthCalculator;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DijkstraRecommendationStrategy implements RecommendationStrategy {
    private final ConnectionStrengthCalculator strengthCalculator;

    @Override
    public String getStrategyName() {
        return "DIJKSTRA";
    }

    @Override
    public List<RecommendationFriendResponse> generateRecommendations(Long userId, Map<Long, Double> socialDistances) {
        return socialDistances.entrySet().stream()
                .map(entry -> createRecommendation(userId, entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(RecommendationFriendResponse::getScore).reversed())
                .collect(Collectors.toList());
    }

    private RecommendationFriendResponse createRecommendation(Long targetUserId, Long candidateId, Double distance) {
        double distanceScore = 1.0 / (1.0 + distance);
        Map<String, Double> factorScores = strengthCalculator.calculateDetailedStrength(targetUserId, candidateId);

        double finalScore = distanceScore * 0.7 + factorScores.values()
                .stream().mapToDouble(Double::doubleValue).sum() * 0.3;

        factorScores.put("socialDistance", distanceScore);

        return RecommendationFriendResponse .builder()
                .recommendedUserId(candidateId)
                .score(finalScore)
                .factorScores(factorScores)
                .createdAt(LocalDateTime.now())
                .build();
    }
}