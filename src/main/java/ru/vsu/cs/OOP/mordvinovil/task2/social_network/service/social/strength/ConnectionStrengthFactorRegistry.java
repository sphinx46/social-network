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

    public Map<String, ConnectionStrengthFactor> getFactorMap() {
        return factors.stream()
                .collect(Collectors.toMap(ConnectionStrengthFactor::getFactorName, Function.identity()));
    }

    public List<ConnectionStrengthFactor> getAllFactors() {
        return factors;
    }

    public ConnectionStrengthFactor getFactor(String name) {
        return getFactorMap().get(name);
    }
}
feat: