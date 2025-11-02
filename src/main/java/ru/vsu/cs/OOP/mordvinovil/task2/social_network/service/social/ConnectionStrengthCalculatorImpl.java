package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils.MutualFriendsCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils.MutualLikesCalculator;

@Service
@RequiredArgsConstructor
public class ConnectionStrengthCalculatorImpl implements ConnectionStrengthCalculator {
    private final MutualFriendsCalculator mutualFriendsCalculator;
    private final MutualLikesCalculator mutualLikesCalculator;

    @Override
    public double calculateConnectionStrength(Long userId, Long targetUserId) {
        double strength = 0.0;

        int mutualLikes = mutualLikesCalculator.calculateMutualLikesCount(userId, targetUserId);
        double normalizedLikesScore = Math.min(mutualLikes / 10.0, 1.0);
        strength += normalizedLikesScore * 0.3;

        int commonFriends = mutualFriendsCalculator.calculateMutualFriendsCount(userId, targetUserId);
        double normalizedFriendsScore = Math.min(commonFriends / 5.0, 1.0);
        strength += normalizedFriendsScore * 0.4;

        return Math.min(1.0, Math.max(0.0, strength));
    }
}
