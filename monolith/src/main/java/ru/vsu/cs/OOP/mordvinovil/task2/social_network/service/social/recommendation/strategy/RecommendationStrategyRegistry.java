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

    /**
     * Получает карту стратегий рекомендаций
     *
     * @return карта стратегий (имя стратегии -> стратегия)
     */
    public Map<String, RecommendationStrategy> getStrategyMap() {
        return strategies.stream()
                .collect(Collectors.toMap(RecommendationStrategy::getStrategyName, strategy -> strategy));
    }

    /**
     * Получает стратегию рекомендаций по умолчанию
     *
     * @return стратегия рекомендаций по умолчанию
     */
    public RecommendationStrategy getDefaultStrategy() {
        return getStrategyMap().get("DIJKSTRA");
    }

    /**
     * Получает стратегию рекомендаций по имени
     *
     * @param name имя стратегии
     * @return стратегия рекомендаций или null если не найдена
     */
    public RecommendationStrategy getStrategy(String name) {
        return getStrategyMap().get(name);
    }

    /**
     * Получает все доступные стратегии рекомендаций
     *
     * @return список всех стратегий рекомендаций
     */
    public List<RecommendationStrategy> getAllStrategies() {
        return strategies;
    }
}