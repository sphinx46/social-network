package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength;

import java.util.Map;

public interface ConnectionStrengthCalculator {
    Map<String, Double> calculateDetailedStrength(Long userId, Long targetUserId);
    double calculateOverallStrength(Long userId, Long targetUserId);
}