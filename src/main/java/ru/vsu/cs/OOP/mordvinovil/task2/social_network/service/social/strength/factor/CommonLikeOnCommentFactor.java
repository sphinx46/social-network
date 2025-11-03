package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.calculator.SocialInteractionCalculator;

@Slf4j
@Component
public final class CommonLikeOnCommentFactor extends AbstractConnectionStrengthFactor {
    private final SocialInteractionCalculator calculator;

    public CommonLikeOnCommentFactor(SocialInteractionCalculator calculator) {
        super(0.18);
        this.calculator = calculator;
    }

    @Override
    public String getFactorName() {
        return "commonComments";
    }

    @Override
    public double calculateStrength(Long userId, Long targetUserId) {
        int commonComments = calculator.calculateCommonLikesOnCommentCount(userId, targetUserId);
        return normalizeScore(commonComments, 10.0);
    }
}