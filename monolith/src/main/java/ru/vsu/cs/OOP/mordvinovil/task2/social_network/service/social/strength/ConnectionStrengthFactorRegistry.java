package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor.ConnectionStrengthFactor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public final class ConnectionStrengthFactorRegistry {
    private final List<ConnectionStrengthFactor> factors;

    /**
     * Получает карту факторов силы связи
     *
     * @return карта факторов (имя фактора -> фактор)
     */
    public Map<String, ConnectionStrengthFactor> getFactorMap() {
        return factors.stream()
                .collect(Collectors.toMap(ConnectionStrengthFactor::getFactorName, Function.identity()));
    }

    /**
     * Получает все доступные факторы силы связи
     *
     * @return список всех факторов силы связи
     */
    public List<ConnectionStrengthFactor> getAllFactors() {
        return factors;
    }

    /**
     * Получает фактор силы связи по имени
     *
     * @param name имя фактора
     * @return фактор силы связи или null если не найден
     */
    public ConnectionStrengthFactor getFactor(String name) {
        return getFactorMap().get(name);
    }
}