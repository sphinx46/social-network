package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

public interface ConnectionStrengthCalculator {
    double calculateConnectionStrength(Long userId, Long targetUserId);
}
