package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.calculator.SocialInteractionCalculator;

@Slf4j
@Component
public final class MutualFriendsFactor extends AbstractConnectionStrengthFactor {
    private final SocialInteractionCalculator calculator;

    public MutualFriendsFactor(SocialInteractionCalculator calculator) {
        super(0.6);
        this.calculator = calculator;
    }

    @Override
    public String getFactorName() {
        return "commonFriends";
    }

    @Override
    public double calculateStrength(Long userId, Long targetUserId) {
        int mutualFriends = calculator.calculateMutualFriendsCount(userId, targetUserId);
        return normalizeScore(mutualFriends, 5.0);
    }
}