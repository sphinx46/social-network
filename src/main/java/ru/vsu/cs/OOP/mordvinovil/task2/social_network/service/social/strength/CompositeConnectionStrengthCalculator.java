package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor.ConnectionStrengthFactor;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public final class CompositeConnectionStrengthCalculator implements ConnectionStrengthCalculator {
    private final ConnectionStrengthFactorRegistry factorRegistry;

    /**
     * Вычисляет детализированную силу связи между пользователями по всем факторам
     *
     * @param userId идентификатор первого пользователя
     * @param targetUserId идентификатор второго пользователя
     * @return карта силы связи по факторам (имя фактора -> значение силы)
     */
    @Override
    public Map<String, Double> calculateDetailedStrength(Long userId, Long targetUserId) {
        return factorRegistry.getAllFactors().stream()
                .collect(Collectors.toMap(
                        ConnectionStrengthFactor::getFactorName,
                        factor -> factor.calculateStrength(userId, targetUserId)
                ));
    }

    /**
     * Вычисляет общую силу связи между пользователями
     *
     * @param userId идентификатор первого пользователя
     * @param targetUserId идентификатор второго пользователя
     * @return общая сила связи (не менее 0.1)
     */
    @Override
    public double calculateOverallStrength(Long userId, Long targetUserId) {
        double strength = factorRegistry.getAllFactors().stream()
                .mapToDouble(factor -> factor.calculateStrength(userId, targetUserId))
                .sum();

        return Math.max(0.1, strength);
    }
}