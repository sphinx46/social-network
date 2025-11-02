package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import java.util.Map;

public interface ConnectionStrengthCalculator {
    Map<String, Double> resultConnectionStrength(Long userId, Long targetUserId);
    Double calculateConnectionStrength(Long userId, Long targetUserId);
}
