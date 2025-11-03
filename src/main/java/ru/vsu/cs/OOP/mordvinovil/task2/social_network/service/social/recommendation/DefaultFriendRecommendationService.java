package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.recommendation.RecommendationFriendResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.graph.SocialGraphService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy.RecommendationStrategy;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy.RecommendationStrategyRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DefaultFriendRecommendationService implements FriendRecommendationService {
    private final SocialGraphService socialGraphService;
    private final RelationshipRepository relationshipRepository;
    private final RecommendationStrategyRegistry strategyRegistry;

    @Override
    public List<RecommendationFriendResponse> getFriendRecommendations(Long targetUserId) {
        RecommendationStrategy strategy = strategyRegistry.getDefaultStrategy();
        Map<Long, Double> socialDistances = socialGraphService.findSocialDistances(targetUserId, 3);
        Set<Long> existingFriendIds = getExistingFriendIds(targetUserId);

        List<RecommendationFriendResponse> recommendations = strategy.generateRecommendations(targetUserId, socialDistances);

        return filterExistingConnections(recommendations, targetUserId, existingFriendIds);
    }

    private Set<Long> getExistingFriendIds(Long userId) {
        return relationshipRepository.findFriendIdsByUserId(userId, FriendshipStatus.ACCEPTED);
    }

    private List<RecommendationFriendResponse > filterExistingConnections(
            List<RecommendationFriendResponse > recommendations,
            Long targetUserId,
            Set<Long> existingFriendIds) {

        return recommendations.stream()
                .filter(rec -> !rec.getRecommendedUserId().equals(targetUserId))
                .filter(rec -> !existingFriendIds.contains(rec.getRecommendedUserId()))
                .collect(Collectors.toList());
    }
}