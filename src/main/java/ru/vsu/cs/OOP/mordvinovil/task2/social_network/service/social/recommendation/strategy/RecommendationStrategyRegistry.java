package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public final class RecommendationStrategyRegistry {
    private final List<RecommendationStrategy> strategies;

    public Map<String, RecommendationStrategy> getStrategyMap() {
        return strategies.stream()
                .collect(Collectors.toMap(RecommendationStrategy::getStrategyName, strategy -> strategy));
    }

    public RecommendationStrategy getDefaultStrategy() {
        return getStrategyMap().get("DIJKSTRA");
    }

    public RecommendationStrategy getStrategy(String name) {
        return getStrategyMap().get(name);
    }

    public List<RecommendationStrategy> getAllStrategies() {
        return strategies;
    }
}