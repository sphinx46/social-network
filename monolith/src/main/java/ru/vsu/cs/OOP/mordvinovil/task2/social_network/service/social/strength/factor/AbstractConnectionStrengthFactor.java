package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractConnectionStrengthFactor implements ConnectionStrengthFactor {
    protected final double weight;

    @Override
    public double getWeight() {
        return weight;
    }

    protected double normalizeScore(double rawScore, double maxScore) {
        return Math.min(rawScore / maxScore, 1.0) * weight;
    }
}