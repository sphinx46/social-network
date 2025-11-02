package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RecommendationFriendResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendRecommendationServiceImpl implements FriendRecommendationService {
    private final DijkstraFriendRecommendationSearchService searchService;
    private final RelationshipRepository relationshipRepository;
    private final ConnectionStrengthCalculator strengthCalculator;

    @Override
    public List<RecommendationFriendResponse> getFriendRecommendations(Long targetUserId) {
        Map<Long, Double> socialDistances = searchService.findSocialDistances(targetUserId, 3);

        Set<Long> existingFriendIds = relationshipRepository
                .findFriendIdsByUserId(targetUserId, FriendshipStatus.ACCEPTED);

        List<RecommendationFriendResponse> recommendations = socialDistances.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(targetUserId))
                .filter(entry -> !existingFriendIds.contains(entry.getKey()))
                .map(entry -> {
                    Long recommendedUserId = entry.getKey();
                    Double distance = entry.getValue();

                    double distanceScore = 1.0 / (1.0 + distance);
                    Map<String, Double> factorScores = strengthCalculator
                            .resultConnectionStrength(targetUserId, recommendedUserId);

                    double finalScore = distanceScore * 0.7 + factorScores.values()
                            .stream().mapToDouble(Double::doubleValue).sum() * 0.3;

                    factorScores.put("socialDistance", distanceScore);

                    return new RecommendationFriendResponse(
                            recommendedUserId,
                            finalScore,
                            factorScores,
                            LocalDateTime.now()
                    );
                })
                .sorted(Comparator.comparing(RecommendationFriendResponse::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return recommendations;
    }
}
