package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor;

public interface ConnectionStrengthFactor {
    String getFactorName();
    double calculateStrength(Long userId, Long targetUserId);
    double getWeight();
}