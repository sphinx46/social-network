package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy;

import lombok.RequiredArgsConstructor;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.recommendation.RecommendationFriendResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.ConnectionStrengthCalculator;

import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractRecommendationStrategy implements RecommendationStrategy {
    protected final ConnectionStrengthCalculator strengthCalculator;

    protected RecommendationFriendResponse createRecommendation(
            Long targetUserId,
            Long candidateId,
            Double distance,
            Map<String, Double> additionalFactors) {

        double distanceScore = calculateDistanceScore(distance);
        Map<String, Double> factorScores = strengthCalculator.calculateDetailedStrength(targetUserId, candidateId);

        if (additionalFactors != null) {
            factorScores.putAll(additionalFactors);
        }

        factorScores.put("socialDistance", distanceScore);

        double finalScore = calculateFinalScore(distanceScore, factorScores);

        return RecommendationFriendResponse.builder()
                .recommendedUserId(candidateId)
                .score(finalScore)
                .factorScores(factorScores)
                .createdAt(LocalDateTime.now())
                .build();
    }

    protected double calculateDistanceScore(double distance) {
        return 1.0 / (1.0 + distance);
    }

    protected double calculateFinalScore(double distanceScore, Map<String, Double> factorScores) {
        double factorsScore = factorScores.values().stream().mapToDouble(Double::doubleValue).sum();
        return distanceScore * 0.7 + factorsScore * 0.3;
    }
}