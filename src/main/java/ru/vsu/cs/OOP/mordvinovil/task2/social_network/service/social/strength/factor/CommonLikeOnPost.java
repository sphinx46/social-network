package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.calculator.SocialInteractionCalculator;

@Slf4j
@Component
public final class CommonLikeOnPost extends AbstractConnectionStrengthFactor {
    private final SocialInteractionCalculator calculator;

    public CommonLikeOnPost(SocialInteractionCalculator calculator) {
        super(0.25);
        this.calculator = calculator;
    }

    @Override
    public String getFactorName() {
        return "commonLikes";
    }

    @Override
    public double calculateStrength(Long userId, Long targetUserId) {
        int commonLikes = calculator.calculateCommonLikesOnPostCount(userId, targetUserId);
        return normalizeScore(commonLikes, 10.0);
    }
}