package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.recommendation.RecommendationFriendResponse;

import java.util.List;
import java.util.Map;

public interface RecommendationStrategy {
    String getStrategyName();
    List<RecommendationFriendResponse> generateRecommendations(Long userId, Map<Long, Double> socialDistances);
}